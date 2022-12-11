package cs451.beb;

import cs451.Host;
import cs451.packet.*;
import cs451.pl.PLSender;
import cs451.network.SocketService;
import cs451.utils.Logger;
import cs451.utils.Sleeper;
import cs451.utils.Stopwatch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

abstract public class BEBSender extends PLSender
{
    public static final int MAX = 8;

    protected final AtomicInteger proposalsToSend;
    private final Map<Integer, Integer> seqMap;

    public BEBSender( SocketService service )
    {
        super( service );
        this.seqMap = new ConcurrentHashMap<>();
        this.proposalsToSend = new AtomicInteger( MAX );
        Stopwatch.init(service.id);
    }

    @Override
    public void addSendQueue( GroupedPacket p )
    {
        seqMap.put( p.dest, p.seq );
        super.addSendQueue( p );
    }

    protected void bebBroadcast( List<PacketContent> contents )
    {
        Logger.log(service.id, "BEBSender", "Broadcasting msg: " + contents);
        GroupedPacket p;
        for ( Host dest : service.getHosts() )
        {
            int seq = seqMap.getOrDefault( dest.getId(), 0 );
            p = new GroupedPacket( seq, service.id, contents, dest.getId() );
            pp2pBroadcast( p );
        }
    }

    abstract public void onPropose( List<PacketContent> contents );

    private void sendOne()
    {
        GroupedPacket p = toSend.poll();
        seqMap.put( p.dest, p.seq );
        pp2pBroadcast( p );
        Logger.log( "BEBSender","toSend - Sent packet " + p );
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
        while ( !service.closed.get() )
        {
            if ( !toSend.isEmpty() )
                sendOne();


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
            {
                Logger.log( "BEBSender","Sent packet " + contents );
                bebBroadcast( contents );
            }

//            if (service.proposals.isEmpty())
//                Stopwatch.stop(service.id);

            Sleeper.release();
        }
    }
}