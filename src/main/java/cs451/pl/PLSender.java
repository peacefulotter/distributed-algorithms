package cs451.pl;

import cs451.network.SeqMsg;
import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.utils.Logger;
import cs451.packet.Packet;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PLSender extends SocketHandler
{
    protected static final int MAX_MSG_PER_PACKET = 8;

    protected final ConcurrentLinkedQueue<Packet> broadcasted;
    protected final AtomicInteger packetsToSent;
    protected final Queue<SeqMsg> queue;
    protected final int nbMessages;

    protected PLReceiver receiver;

    public PLSender( SocketService service )
    {
        super(service);
        this.nbMessages = service.nbMessages;
        this.broadcasted = new ConcurrentLinkedQueue<>();
        this.packetsToSent = new AtomicInteger(0);
        this.queue = getQueue();
    }

    public void setPacketsToSent( int nb )
    {
        packetsToSent.set( nb );
    }

    public void setReceiver( PLReceiver receiver )
    {
        this.receiver = receiver;
    }

    public boolean pp2pBroadcast( Packet packet )
    {
        boolean sent = sendPacket( packet );
        if ( !sent ) return false;
        if ( !broadcasted.contains( packet ) ) broadcasted.add( packet );
        receiver.addPacketTimeout( packet );
        Logger.log( "PLSender", "Sent packet " + packet + " to " + packet.getDestId() );
        return true;
    }

    protected Queue<SeqMsg> getQueue()
    {
        // TODO: MAX SUBMIT TASKS
        Queue<SeqMsg> queue = new ArrayDeque<>();
        int seqNr;
        for ( seqNr = 1; seqNr < nbMessages; seqNr += MAX_MSG_PER_PACKET )
        {
            // try to send a maximum of 8 messages per packet
            int messages = Math.min(MAX_MSG_PER_PACKET, nbMessages - seqNr + 1);
            queue.add( new SeqMsg( seqNr, messages ) );
        }
        return queue;
    }
}
