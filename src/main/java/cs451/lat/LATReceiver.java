package cs451.lat;

import cs451.beb.BEBReceiver;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.packet.SetPacket;

import java.util.HashSet;
import java.util.Set;

import static cs451.lat.LATService.SEND_PROPOSAL;

public class LATReceiver extends BEBReceiver
{
    private Set<Integer> accepted_value;

    private final LATService lat;

    public LATReceiver( SocketService service, LATService lat )
    {
        super( service );
        this.lat = lat;
        this.accepted_value = new HashSet<>();
    }

    public void onPacket( Packet p )
    {
        if ( p.type == PacketTypes.LAT_PROP )
            onProposal( p.seq, ((SetPacket) p).proposal, p.src );
        else if ( p.type == PacketTypes.LAT_NACK )
            onLatNack( p.seq, ((SetPacket) p).proposal );
        else if ( p.type == PacketTypes.LAT_ACK )
            onLatAck( p.seq );
        else
            super.onPacket( p );
    }

    /* upon reception of <ACK, proposal_number>
     *  s.t. proposal_number == active_proposal_number */
    public void onLatAck( int proposal_number )
    {
        if ( proposal_number != lat.active_proposal_number.get() )
            return;

        lat.onAck((LATSender) sender);
    }

    /* upon reception of <NACK, proposal_number, value>
     *  s.t. proposal_number == active_proposal_number */
    public void onLatNack( int proposal_number, Proposal value )
    {
        if ( proposal_number != lat.active_proposal_number.get() )
            return;

        lat.onNack((LATSender) sender, value );
    }

    // acceptor
    private void onAckProposal( int proposal_number, Proposal proposed_value, int src )
    {
        accepted_value = proposed_value;
        ((LATSender) sender).sendAck( proposal_number, src );
    }

    private void onNackProposal( int proposal_number, Proposal proposed_value, int src )
    {
        accepted_value.addAll( proposed_value );
        ((LATSender) sender).sendNack( proposed_value, proposal_number, src );
    }

    private void onProposal( int proposal_number, Proposal proposed_value, int src )
    {
        System.out.println("onProposal: proposed_value=" + proposed_value + "  , accepted_value=" + accepted_value );
        boolean isSubset = proposed_value.containsAll( accepted_value );
        if ( isSubset )
            onAckProposal( proposal_number, proposed_value, src );
        else
            onNackProposal( proposal_number, proposed_value, src );
    }
}
