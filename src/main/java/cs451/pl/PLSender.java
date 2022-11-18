package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.packet.Message;
import cs451.utils.Logger;
import cs451.packet.Packet;
import cs451.utils.WithGC;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PLSender extends SocketHandler
{
    // TODO: GC of acknowledged

    protected static final int PACKETS_TO_SEND = 3;

    private final Timer timer;

    protected final ConcurrentLinkedQueue<Message> toBroadcast;
    protected final ConcurrentLinkedQueue<Packet> toSend;
    protected final ConcurrentSkipListSet<Integer> acknowledged;

    protected final AtomicInteger packetsToSend;

    protected PLReceiver receiver;

    public PLSender( SocketService service )
    {
        super(service);
        this.timer = new Timer("Timer");
        this.toBroadcast = new ConcurrentLinkedQueue<>();
        this.toSend = new ConcurrentLinkedQueue<>();
        this.acknowledged = new ConcurrentSkipListSet<>();
        this.packetsToSend = new AtomicInteger( PACKETS_TO_SEND );
    }

    public void addBroadcastQueue( Message msg )
    {
        this.toBroadcast.add( msg );
    }

    public void addSendQueue( Packet p )
    {
        this.toSend.add( p );
    }

    public void setReceiver( PLReceiver receiver )
    {
        this.receiver = receiver;
    }

    public void addTimeoutTask( Packet packet )
    {
        int destId = packet.getDestId();
        TimerTask task = new TimerTask() {
            public void run() {
            Logger.log(  "Scheduler fired for " + packet );
            // same packet but different type to do the comparisons in acknowledged.contains
            Packet ackPacket = Packet.createACKPacket( packet );
            if ( !service.closed.get() && !acknowledged.contains( ackPacket.hashCode() ) )
            {
                service.timeout.increase( destId );
                addSendQueue( packet );
            }
            cancel();
            }
        };
        timer.schedule( task, service.timeout.get( destId ) );
    }

    public void onDeliver( Packet packet )
    {
        if ( packet.getOrigin() == service.id )
            packetsToSend.incrementAndGet();
    }

    protected void register( Message m )
    {
        service.register( new Packet( m, service.getSelf() ) );
    }

    public void pp2pBroadcast( Packet packet )
    {
        if ( !sendPacket( packet ) )
            return;
        addTimeoutTask( packet );
    }

    protected void onAcknowledge( Packet packet )
    {
        int hc = packet.hashCode();
        if ( acknowledged.contains( hc ) )
            return;

        Logger.log(  "Acknowledged " + packet );

        acknowledged.add( hc );
    }
}
