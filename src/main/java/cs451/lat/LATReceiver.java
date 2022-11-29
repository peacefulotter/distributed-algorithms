package cs451.lat;

import cs451.beb.BEBReceiver;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.packet.SetPacket;
import cs451.utils.Logger;

import java.util.HashSet;
import java.util.Set;

import static cs451.lat.LATService.SEND_PROPOSAL;

public class LATReceiver extends BEBReceiver
{
    // TODO: Set<LATService>
    // beb proposal -> round++
    // service = services.get(round)

    private Set<Integer> accepted_value;

    private final LATService lat;

    public LATReceiver( SocketService service, LATService lat )
    {
        super( service );
        this.lat = lat;
        this.accepted_value = new HashSet<>();
    }

    protected void reset()
    {
        accepted_value.clear();
    }

    public void onPacket( Packet p )
    {
        Logger.log(service.id, "LATReceiver", p);
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
        Logger.log(service.id, "LATReceiver", "on ACK, prop_nb=" + proposal_number + ", active_prop_nb=" + lat.active_proposal_number.get() );
        if ( proposal_number != lat.active_proposal_number.get() )
            return;

        lat.onAck((LATSender) sender, this);
    }

    /* upon reception of <NACK, proposal_number, value>
     *  s.t. proposal_number == active_proposal_number */
    public void onLatNack( int proposal_number, Proposal value )
    {
        Logger.log(service.id, "LATReceiver", "on NACK, prop_nb=" + proposal_number + ", active_prop_nb=" + lat.active_proposal_number.get() );
        if ( proposal_number != lat.active_proposal_number.get() )
            return;

        lat.onNack((LATSender) sender, value );
    }

    // acceptor
    private void onAckProposal( int proposal_number, Proposal proposed_value, int src )
    {
        accepted_value = proposed_value;
        Logger.log(service.id, "LATReceiver", "on ACK Proposal: new acc_value=" + accepted_value );
        ((LATSender) sender).sendAck( proposal_number, src );
    }

    private void onNackProposal( int proposal_number, Proposal proposed_value, int src )
    {
        accepted_value.addAll( proposed_value );
        Logger.log(service.id, "LATReceiver", "on NACK Proposal: new acc_value=" + accepted_value );
        ((LATSender) sender).sendNack( accepted_value, proposal_number, src );
    }

    private void onProposal( int proposal_number, Proposal proposed_value, int src )
    {
        boolean isSubset = proposed_value.containsAll( accepted_value );
        Logger.log(service.id, "LATReceiver", "onProposal: number=" + proposal_number + ", prop_value=" + proposed_value + ", acc_value=" + accepted_value + ", from=" + src + ", subset?=" + isSubset);
        if ( isSubset )
            onAckProposal( proposal_number, proposed_value, src );
        else
            onNackProposal( proposal_number, proposed_value, src );
    }
}
