package cs451.beb;

import cs451.Host;
import cs451.network.SeqMsg;
import cs451.packet.PacketTypes;
import cs451.pl.PLSender;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.utils.Logger;
import cs451.utils.Sleeper;

public class BEBSender extends PLSender
{
    public BEBSender( SocketService service )
    {
        super( service );
    }

    protected Packet getPacket( Host dest, SeqMsg sm )
    {
        return new Packet( PacketTypes.BROADCAST, sm.seqNr, service.id, dest, sm.messages );
    }

    public boolean bebBroadcast( SeqMsg seqMsg )
    {
        boolean sent = true;
        for ( Host dest: service.getHosts() )
        {
            Packet packet = getPacket( dest, seqMsg );
            boolean broadcasted = pp2pBroadcast( packet );
            sent = sent && broadcasted;
        }
        return sent;
    }

    public void onAck( Packet packet )
    {
        /*Long time = broadcasted.get( packet );
        if ( time == null )
        {
            broadcasted.replaceAll( (p, l) -> timeout - System.nanotime().toMS() );
        }*/
    }

    @Override
    public void run()
    {
        while ( !service.closed.get() && !queue.isEmpty() )
        {
            if ( packetsToSent.get() > 0 )
            {
                SeqMsg seqMsg = queue.poll();
                if ( bebBroadcast( seqMsg ) )
                {
                    packetsToSent.decrementAndGet();
                }
            }
            Sleeper.release();
        }
        Logger.log("Sender", " Done");
    }
}

