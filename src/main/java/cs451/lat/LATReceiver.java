package cs451.lat;

import cs451.beb.BEBReceiver;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;

import java.util.Set;

public class LATReceiver extends BEBReceiver
{
    private Set<Integer> accepted_value;

    private final LATService lat;

    public LATReceiver( SocketService service, LATService lat )
    {
        super( service );
        this.lat = lat;
    }

    public void onPacket( Packet packet )
    {
        if ( packet.getType() == PacketTypes.LAT_PRO )
            onProposal();
        else if ( packet.getType() == PacketTypes.LAT_ACK )
            onLatAck();
        else if ( packet.getType() == PacketTypes.LAT_NACK )
            onLatNack();
        else
            super.onPacket( packet );
    }

    public void onLatAck( int proposal_number )
    {
        if ( proposal_number != lat.active_proposal_number )
            return;
        lat.incAckCount();
    }

    public void onLatNack(int proposal_number, Set<Integer> value)
    {
        if ( proposal_number != lat.active_proposal_number )
            return;

        lat.proposedValue.addAll( value );
        lat.incNackCount();
    }

    // acceptor
    private void onProposal( Set<Integer> proposed_value, int proposal_number )
    {
        if ( proposed_value.containsAll( accepted_value ))
        {
            accepted_value = proposed_value;
            ((LATSender) sender).sendAck(proposal_number );
        }
        else
        {
            accepted_value.addAll( proposed_value );
            ((LATSender) sender).sendNack( proposed_value, proposal_number );
        }
    }
}
