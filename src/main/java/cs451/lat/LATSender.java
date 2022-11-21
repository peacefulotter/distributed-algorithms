package cs451.lat;

import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.Message;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;

import java.util.Set;

public class LATSender extends BEBSender
{
    private boolean status = false;

    private final LATService lat;

    public LATSender( SocketService service, LATService lat )
    {
        super( service );
        this.lat = lat;
    }

    // TODO: this should override broadcast
    public void propose( Set<Integer> proposal )
    {
        lat.proposedValue = proposal;
        lat.active_proposal_number++;
        status = true;
        Message msg = new Message(  );
        bebBroadcast( msg );
    }

    public void sendProposal()
    {
        Message msg = new Message( lat.proposedValue, lat.active_proposal_number );
        addBroadcastQueue( msg );
    }

    public void sendAck( int proposal_number )
    {
        // TODO:
        Packet p = new Packet( PacketTypes.LAT_ACK, proposal_number );
        addSendQueue( p );
    }

    public void sendNack( Set<Integer> accepted_value,  int proposal_number  )
    {
        // TODO:
        Packet p = new Packet( PacketTypes.LAT_NACK, proposal_number, accepted_value );
        addSendQueue( p );
    }
}
