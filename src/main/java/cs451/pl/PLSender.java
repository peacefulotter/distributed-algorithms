package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.packet.SetMessage;
import cs451.utils.Logger;
import cs451.packet.Packet;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PLSender extends SocketHandler
{
    private final Timer timer;

    protected final ConcurrentLinkedQueue<SetMessage> toBroadcast;
    protected final ConcurrentLinkedQueue<Packet> toSend;
    protected final ConcurrentSkipListSet<Packet> pendingAck;

    protected final AtomicInteger proposalsToSend;

    protected PLReceiver receiver;

    public PLSender( SocketService service )
    {
        super(service);
        this.timer = new Timer("Timer");
        this.toBroadcast = new ConcurrentLinkedQueue<>();
        this.toSend = new ConcurrentLinkedQueue<>();
        this.pendingAck = new ConcurrentSkipListSet<>( Packet.getAckComparator() );
        this.proposalsToSend = new AtomicInteger( 1 );
    }

    public void addBroadcastQueue( SetMessage msg )
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
            if ( !service.closed.get() && pendingAck.contains( packet ) )
            {
                Logger.log(  "Scheduler fired for " + packet );
                service.timeout.increase( destId );
                addSendQueue( packet );
            }
            cancel();
            }
        };
        timer.schedule( task, service.timeout.get( destId ) );
    }

    public void pp2pBroadcast( Packet packet )
    {
        if ( !sendPacket( packet ) )
            return;
        System.out.println("pl: " + packet);
        pendingAck.add( packet );
        addTimeoutTask( packet );
    }

    protected void onAcknowledge( Packet packet )
    {
        System.out.println("ack: " + packet);
        if ( !pendingAck.contains( packet ) )
            return;

        Logger.log(  "Acknowledged " + packet );
        pendingAck.remove( packet );
        service.timeout.decrease( packet.getSrc() );
    }
}
