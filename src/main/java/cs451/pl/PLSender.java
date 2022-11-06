package cs451.pl;

import cs451.network.SeqMsg;
import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.utils.Logger;
import cs451.packet.Packet;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PLSender extends SocketHandler
{
    // TODO: garbage collection

    protected static final int MAX_MSG_PER_PACKET = 8;
    protected static final int PACKETS_TO_SEND = 3;

    private final Timer timer;

    protected final ConcurrentLinkedQueue<Packet> broadcasted, acknowledged;
    protected final AtomicInteger broadcastedSize; // since broadcasted.size() executes in O(n)
    protected final AtomicInteger packetsToSend;
    protected final Queue<SeqMsg> queue;
    protected final int nbMessages;

    protected PLReceiver receiver;

    public PLSender( SocketService service )
    {
        super(service);
        this.nbMessages = service.nbMessages;
        this.timer = new Timer("Timer");
        this.broadcasted = new ConcurrentLinkedQueue<>();
        this.acknowledged = new ConcurrentLinkedQueue<>();
        this.broadcastedSize = new AtomicInteger(0);
        this.packetsToSend = new AtomicInteger(PACKETS_TO_SEND);
        this.queue = getQueue();
    }

    public void setReceiver( PLReceiver receiver )
    {
        this.receiver = receiver;
    }

    public void addTimeoutTask( Packet packet )
    {
        TimerTask task = new TimerTask() {
            public void run() {
                // TODO: max try
                Logger.log(  "Scheduler fired for " + packet );
                // Logger.log( acknowledged.size() + " - " + acknowledged.toString() );
                // same packet but different type to do the comparisons in acknowledged.contains
                Packet ackPacket = Packet.createACKPacket( packet );
                // Logger.log(ackPacket.toString());
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
    abstract protected void onBroadcast(Packet packet);

    public boolean pp2pBroadcast( Packet packet )
    {
        if ( !sendPacket( packet ) )
            return false;

        if ( !broadcasted.contains( packet ) )
        {
            broadcasted.add( packet );
            broadcastedSize.incrementAndGet();
            onBroadcast( packet );
        }

        addTimeoutTask( packet );

        Logger.log( "Sent packet " + packet );
        return true;
    }

    public void onAck( Packet packet )
    {
        if ( !acknowledged.contains( packet ) )
        {
            Logger.log(  "Acknowledged " + packet );
            acknowledged.add( packet );
            packetsToSend.incrementAndGet();
        }
    }

    protected Queue<SeqMsg> getQueue()
    {
        // TODO: getNext(seqNr, nbMessages)
        Queue<SeqMsg> queue = new ArrayDeque<>();
        int seqNr;
        for ( seqNr = 1; seqNr < nbMessages + 1; seqNr += MAX_MSG_PER_PACKET )
        {
            // try to send a maximum of 8 messages per packet
            int messages = Math.min(MAX_MSG_PER_PACKET, nbMessages - seqNr + 1);
            queue.add( new SeqMsg( seqNr, messages ) );
        }
        return queue;
    }
}
