package cs451.beb;

import cs451.Host;
import cs451.packet.*;
import cs451.pl.PLSender;
import cs451.network.SocketService;
import cs451.utils.Logger;
import cs451.utils.Pair;
import cs451.utils.Sleeper;
import cs451.utils.Stopwatch;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class BEBSender extends PLSender
{
    public static final int MAX = 8;

    protected final AtomicInteger proposalsToSend;

    public BEBSender( SocketService service )
    {
        super( service );
        this.proposalsToSend = new AtomicInteger( MAX );
        Stopwatch.init(service.id);
    }

    protected void bebBroadcast( int seq, List<PacketContent> contents )
    {
        Logger.log(service.id, "BEBSender", "Broadcasting msg: " + contents);
        for ( Host dest : service.getHosts() )
            pp2pSend( seq, contents, dest.getId() );
    }

    abstract public void onPropose( List<PacketContent> contents );

    private int sendOne( int seq )
    {
        Pair<List<PacketContent>, Integer> p = responseSend.poll();
        if ( p == null ) return seq;
        Logger.log( service.id,"BEBSender","responseSend - Sent packet " + p );
        pp2pSend( seq, p.getA(), p.getB() );
        return seq + 1;
    }

    private void sendScheduler()
    {
        GroupedPacket p = schedulerSend.poll();
        if ( p == null ) return;
        Logger.log( service.id, "BEBSender", "SchedulerQueue - Sending " + p.minify() );
        pp2pSend( p );
    }

    private int formContents( List<PacketContent> contents, Queue<PacketContent> q, int max )
    {
        int nbToSend = Math.min( max, MAX - contents.size() );
        for ( int i = 0; i < nbToSend; i++ )
            contents.add( q.poll() );
        return nbToSend;
    }

    @Override
    public void run()
    {
        int seq = 0;
        while ( !service.closed.get() )
        {
            seq = sendOne(seq);
            sendScheduler();

            List<PacketContent> contents = new ArrayList<>( MAX );
            if ( !toBroadcast.isEmpty() )
                formContents( contents, toBroadcast, toBroadcast.size() );

            int toPropose = proposalsToSend.get();
            int max = Math.min( toPropose, service.proposals.size() );
            if ( max > 0 )
            {
                int nbProposed = formContents( contents, service.proposals, max );
                proposalsToSend.addAndGet( -nbProposed );
                onPropose( contents );
            }

            if ( contents.size() > 0 )
                bebBroadcast( seq++, contents );

            contents.clear();

//            if (service.proposals.isEmpty())
//                Stopwatch.stop(service.id);

            Sleeper.release();
        }
    }
}