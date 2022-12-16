package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.packet.*;
import cs451.utils.Logger;
import cs451.utils.Pair;

import java.net.DatagramPacket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class PLSender extends SocketHandler
{
    private final Timer timer;

    protected final ConcurrentLinkedQueue<PacketContent> toBroadcast;
    protected final ConcurrentLinkedQueue<Pair<List<PacketContent>, Integer>> responseSend;
    protected final ConcurrentSkipListSet<MiniPacket> pendingAck;

    protected PLReceiver receiver;

    public PLSender( SocketService service )
    {
        super(service);
        this.timer = new Timer("Timer");
        this.toBroadcast = new ConcurrentLinkedQueue<>();
        this.responseSend = new ConcurrentLinkedQueue<>();
        this.pendingAck = new ConcurrentSkipListSet<>();
    }

    public void setReceiver( PLReceiver receiver )
    {
        this.receiver = receiver;
    }

    public void addBroadcastQueue( PacketContent c )
    {
        this.toBroadcast.add( c );
    }

    public void addResponseQueue( List<PacketContent> c, int dest )
    {
        this.responseSend.add( new Pair<>( c, dest ) );
    }

    public void addTimeoutTask( GroupedPacket p )
    {
        Logger.log(service.id, "PLSender", "Adding " + p + " to scheduler");
        final GroupedPacket scheduledPacket = new GroupedPacket( p );
        TimerTask task = new TimerTask() {
            public void run() {
                if ( !service.closed.get() && pendingAck.contains( scheduledPacket.minify() ) )
                {
                    Logger.log(  service.id, "Scheduler fired for " + scheduledPacket );
                    service.timeout.increase( scheduledPacket.dest );
                    pp2pSend( scheduledPacket );
                }
                cancel();
            }
        };
        timer.schedule( task, service.timeout.get( p.dest ) );
    }

    public void pp2pSend( GroupedPacket p )
    {
        DatagramPacket dp = PacketParser.format( p );
        if ( !service.sendPacket( dp, p.dest ) )
            return;
        Logger.log(service.id, "PLSender", "content size: " + p.contents.size() + " " + p.minify());
        pendingAck.add( p.minify() );
        addTimeoutTask( p );
    }

    public void pp2pSend( int seq, List<PacketContent> c, int dest )
    {
        GroupedPacket p = new GroupedPacket( seq, service.id, c, dest );
        pp2pSend( p );
    }

    public void onAcknowledge( MiniPacket mp )
    {
        Logger.log(service.id, "PLSender", "Ack: " + mp );
        if ( !pendingAck.remove( mp ) )
            return;

        Logger.print(service.id,"PLSender", "Acknowledged " + mp );
        service.timeout.decrease( mp.dest );
    }
}
