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
    protected final ConcurrentLinkedQueue<Pair<List<PacketContent>, Integer>> toSend;
    protected final ConcurrentSkipListSet<MiniPacket> pendingAck;

    protected PLReceiver receiver;

    public PLSender( SocketService service )
    {
        super(service);
        this.timer = new Timer("Timer");
        this.toBroadcast = new ConcurrentLinkedQueue<>();
        this.toSend = new ConcurrentLinkedQueue<>();
        this.pendingAck = new ConcurrentSkipListSet<>();
    }

    public void addBroadcastQueue( PacketContent c )
    {
        this.toBroadcast.add( c );
    }

    public void addSendQueue( List<PacketContent> c, int dest )
    {
        this.toSend.add( new Pair<>( c, dest ) );
    }

    public void addSendQueue( GroupedPacket p )
    {
        addSendQueue( p.contents, p.dest );
    }

    public void setReceiver( PLReceiver receiver )
    {
        this.receiver = receiver;
    }

    public void addTimeoutTask( GroupedPacket p )
    {
        TimerTask task = new TimerTask() {
            public void run() {
            if ( !service.closed.get() && pendingAck.contains( p.minify() ) )
            {
                Logger.log(  "Scheduler fired for " + p );
                service.timeout.increase( p.dest );
                addSendQueue( p );
            }
            cancel();
            }
        };
        timer.schedule( task, service.timeout.get( p.dest ) );
    }

    public void pp2pSend( int seq, List<PacketContent> c, int dest )
    {
        GroupedPacket p = new GroupedPacket( seq, service.id, c, dest );
        DatagramPacket dp = PacketParser.format( p );
        if ( !service.sendPacket( dp, dest ) )
            return;
        Logger.log("PLSender", p);
        pendingAck.add( p.minify() );
        addTimeoutTask( p );
    }

    public void onAcknowledge( MiniPacket p )
    {
        MiniPacket mp = p.revert();
        Logger.log("PLSender", "Ack: " + mp);
        if ( !pendingAck.remove( mp ) )
            return;

        Logger.log(  "PLSender", "Acknowledged " + mp );
        service.timeout.decrease( p.src );
    }
}
