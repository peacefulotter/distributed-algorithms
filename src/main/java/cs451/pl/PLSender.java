package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.utils.Logger;
import cs451.packet.Packet;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PLSender extends SocketHandler
{
    protected static final int PACKETS_TO_SEND = 3;

    private final Timer timer;

    // TODO: GC of acknowledged
    protected final ConcurrentLinkedQueue<Packet> broadcasted, acknowledged;
    protected final AtomicInteger packetsToSend;

    protected PLReceiver receiver;

    public PLSender( SocketService service )
    {
        super(service);
        this.timer = new Timer("Timer");
        this.broadcasted = new ConcurrentLinkedQueue<>();
        this.acknowledged = new ConcurrentLinkedQueue<>();
        this.packetsToSend = new AtomicInteger(PACKETS_TO_SEND * service.getNbHosts());
    }

    public void setReceiver( PLReceiver receiver )
    {
        this.receiver = receiver;
    }

    public void addTimeoutTask( Packet packet )
    {
        TimerTask task = new TimerTask() {
            public void run() {
                Logger.log(  "Scheduler fired for " + packet );
                // same packet but different type to do the comparisons in acknowledged.contains
                Packet ackPacket = Packet.createACKPacket( packet );
                if ( !service.closed.get() && !acknowledged.contains( ackPacket ) )
                {
                    pp2pBroadcast( packet );
                    service.timeout.increase();
                }
                cancel();
            }
        };
        timer.schedule( task, service.timeout.get() );
    }

    /**
     * Called when broadcasting the given packet succeeded, and it is not a retransmit.
     * Definition can register the packet to the FileHandler
     */
    protected boolean onBroadcast(Packet packet)
    {
        if ( broadcasted.contains( packet ) )
            return false;

        broadcasted.add( packet );
        return true;
    }

    protected void onNewBroadcast( Packet packet )
    {
        service.register( packet );
    }

    public boolean pp2pBroadcast( Packet packet )
    {
        if ( !sendPacket( packet ) )
            return false;

        if ( onBroadcast( packet ) )
            onNewBroadcast( packet );

        addTimeoutTask( packet );

        Logger.log( "Sent packet " + packet );
        return true;
    }

    /**
     * Can be redefined by children to execute some additional steps
     *  when receiving an ACK packet
     */
    protected void onAcknowledge( Packet packet )
    {
        if ( acknowledged.contains( packet ) )
            return;

        Logger.log(  "Acknowledged " + packet );

        acknowledged.add( packet );
        broadcasted.remove( Packet.createBRCPacket( packet ) );

        // TODO: not a fan of that
        packetsToSend.incrementAndGet();
    }
}
