package cs451.beb;

import cs451.Host;
import cs451.network.SeqMsg;
import cs451.packet.PacketTypes;
import cs451.pl.PLSender;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.utils.Logger;
import cs451.utils.Sleeper;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BEBSender extends PLSender
{
    private final ConcurrentLinkedQueue<Integer> seqBroadcasted;

    public BEBSender( SocketService service )
    {
        super( service );
        this.seqBroadcasted = new ConcurrentLinkedQueue<>();
    }

    protected Packet getPacket( Host dest, SeqMsg sm )
    {
        return new Packet( PacketTypes.BROADCAST, sm.seqNr, service.id, dest, sm.messages );
    }

    public boolean bebBroadcast( SeqMsg seqMsg )
    {
        boolean sent = true;
        for ( Host dest : service.getHosts() )
        {
            Packet packet = getPacket( dest, seqMsg );
            boolean broadcasted = pp2pBroadcast( packet );
            sent = sent && broadcasted;
        }
        return sent;
    }

    @Override
    protected void onBroadcast( Packet packet )
    {
        int seq = packet.getSeqNr();
        if ( !seqBroadcasted.contains( seq ) )
        {
            seqBroadcasted.add( seq );
            service.register( packet );
        }
    }

    @Override
    public void run()
    {
        int nbSequences = nbMessages / MAX_MSG_PER_PACKET + ((nbMessages % MAX_MSG_PER_PACKET > 0) ? 1 : 0);
        int packetsToBroadcast = nbSequences * service.getNbHosts();
        Logger.log( "BEBSender", nbSequences + " " + packetsToBroadcast );

        SeqMsg seqMsg = getFirst();
        while (
            !service.closed.get() &&
            broadcastedSize.get() < packetsToBroadcast
        )
        {
            if ( packetsToSend.get() > 0 )
            {
                if ( bebBroadcast( seqMsg ) )
                    packetsToSend.decrementAndGet();
                seqMsg = getNext( seqMsg.seqNr );
            }
            Sleeper.release();
        }
        Logger.log( "BEBSender", " Done" );
    }
}

