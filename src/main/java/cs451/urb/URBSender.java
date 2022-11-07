package cs451.urb;

import cs451.beb.BEBSender;
import cs451.network.SeqMsg;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.utils.Logger;
import cs451.utils.Sleeper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class URBSender extends BEBSender
{
    private final ConcurrentMap<Integer, Integer> majorityAck;

    public URBSender( SocketService service )
    {
        super( service );
        this.majorityAck = new ConcurrentHashMap<>();
    }

    @Override
    protected void onAcknowledged( Packet packet )
    {
        // FIXME: need a majority?
        Integer seq = packet.getSeqNr();
        int ack = majorityAck.getOrDefault( seq, 0 );
        majorityAck.put( seq, ack + 1 );
        // if ( ack >= service.getNbHosts() / 2 )
    }

    public boolean urbBroadcast( SeqMsg seqMsg )
    {
        return bebBroadcast( seqMsg );
    }

    @Override
    protected void onBroadcast( Packet packet )
    {
        super.onBroadcast( packet );
    }

    // TODO: dont need to redefine this for all children
    @Override
    public void run()
    {
        int max = SeqMsg.MAX_MSG_PER_PACKET;
        int nbSequences = nbMessages / max + ((nbMessages % max > 0) ? 1 : 0);
        int packetsToBroadcast = nbSequences * service.getNbHosts();
        Logger.log( "URBSender", nbSequences + " " + packetsToBroadcast );

        SeqMsg seqMsg = SeqMsg.getFirst( nbMessages );
        while (
            !service.closed.get() &&
            broadcastedSize.get() < packetsToBroadcast
        )
        {
            if ( packetsToSend.get() > 0 )
            {
                if ( urbBroadcast( seqMsg ) )
                    packetsToSend.decrementAndGet();
                seqMsg = seqMsg.getNext( nbMessages );
            }
            Sleeper.release();
        }
        Logger.log( "URBSender", " Done" );
    }
}

