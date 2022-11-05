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
        Logger.log( "BEBSender", queue.toString() );
        int targetAckSize = queue.size() * service.getHosts().size();
        while (
            !service.closed.get() &&
            acknowledgedSize.get() < targetAckSize
        )
        {
            if ( packetsToSend.get() > 0 )
            {
                SeqMsg seqMsg = queue.poll();
                if ( bebBroadcast( seqMsg ) )
                    packetsToSend.decrementAndGet();
            }
            System.out.println(acknowledged.size() + " " + acknowledgedSize + " -> " + targetAckSize);
            Sleeper.release();
        }
        Logger.log( "BEBSender", " Done" );
    }
}

