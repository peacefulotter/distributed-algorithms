package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.packet.*;
import cs451.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PLSender extends SocketHandler
{
    private final Timer timer;

    protected final ConcurrentLinkedQueue<PacketContent> toBroadcast;
    protected final ConcurrentLinkedQueue<GroupedPacket> toSend;
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

    public void addSendQueue( GroupedPacket p )
    {
        this.toSend.add( p );
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

    public void pp2pBroadcast( GroupedPacket p )
    {
        if ( !sendPacket( p ) )
            return;
        Logger.log("PLSender", p);
        pendingAck.add( p.minify() );
        addTimeoutTask( p );
    }

    public void onAcknowledge( GroupedPacket p )
    {
        Logger.log("PLSender", "Ack: " + p);
        MiniPacket mini = p.minify();
        if ( !pendingAck.contains( mini ) )
            return;

        Logger.log(  "PLSender", "Acknowledged " + p );
        pendingAck.remove( mini );
        service.timeout.decrease( p.getSrc() );
    }
}
