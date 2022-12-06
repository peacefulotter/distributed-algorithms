package cs451.lat;

import cs451.beb.BEBReceiver;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.packet.PacketClass;
import cs451.packet.PacketTypes;
import cs451.packet.SetPacket;
import cs451.utils.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LATReceiver extends BEBReceiver
{
    // round -> decision
    private final Map<Integer, Proposal> decided;
    // round -> service
    private final ConcurrentMap<Integer, LATService> latServices;

    public LATReceiver( SocketService service )
    {
        super( service );
        this.decided = new HashMap<>();
        this.latServices = new ConcurrentHashMap<>();
    }

    public void setDecided( int round, Proposal decision )
    {
        this.decided.put( round, decision );
        this.latServices.remove( round );
    }

    public LATService getLat( int round )
    {
        LATService lat = latServices.get( round );
        if ( lat == null && !decided.containsKey( round ) )
        {
            lat = new LATService( service, round );
            latServices.put( round, lat );
        }
        return lat;
    }

    public void onPacket( Packet p )
    {
        int round = p.round;
        Logger.log(service.id, "LATReceiver round=" + round, p);

        LATService lat = getLat( round );
        // Round already decided and receiving a LAT_PROP
        if (
            lat == null &&
            decided.containsKey( round ) &&
            p.type == PacketTypes.LAT_PROP
        )
        {
            Proposal decision = decided.get( round );
            onDecidedProposal( round, decision, p.prop_nb, ((SetPacket) p).proposal, p.src );
        }
        // Round not decided yet -> participate to agreement
        else if ( lat != null )
        {
            if ( p.type == PacketTypes.LAT_PROP )
                onProposal( lat, p.prop_nb, ((SetPacket) p).proposal, p.src );
            else if ( p.type == PacketTypes.LAT_NACK )
                onLatNack( lat, p.prop_nb, ((SetPacket) p).proposal );
            else if ( p.type == PacketTypes.LAT_ACK )
                onLatAck( lat, p.prop_nb );
        }

        super.onPacket( p );
    }

    /* upon reception of <ACK, proposal_number>
     *  s.t. proposal_number == active_proposal_number */
    public void onLatAck( LATService lat, int proposal_number )
    {
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on ACK, prop_nb=" + proposal_number + ", active_prop_nb=" + lat.active_proposal_number.get() );
        if ( proposal_number != lat.active_proposal_number.get() )
            return;

        lat.onAck((LATSender) sender, this);
    }

    /* upon reception of <NACK, proposal_number, value>
     *  s.t. proposal_number == active_proposal_number */
    public void onLatNack( LATService lat, int proposal_number, Proposal value )
    {
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on NACK, prop_nb=" + proposal_number + ", active_prop_nb=" + lat.active_proposal_number.get() );
        if ( proposal_number != lat.active_proposal_number.get() )
            return;

        lat.onNack((LATSender) sender, value );
    }

    // acceptor
    private void onAckProposal( LATService lat, int proposal_number, Proposal proposed_value, int src )
    {
        lat.accepted_value = proposed_value;
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on ACK Proposal: new acc_value=" + lat.accepted_value );
        ((LATSender) sender).sendAck( lat.round, proposal_number, src );
    }

    private void onNackProposal( LATService lat, int proposal_number, Proposal proposed_value, int src )
    {
        lat.accepted_value.addAll( proposed_value );
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on NACK Proposal: new acc_value=" + lat.accepted_value );
        ((LATSender) sender).sendNack( lat.round, proposal_number, src, lat.accepted_value );
    }

    private void onProposal( LATService lat, int proposal_number, Proposal proposed_value, int src )
    {
        boolean isSubset = proposed_value.containsAll( lat.accepted_value );
        Logger.log(service.id, "LATReceiver round=" + lat.round, "onProposal: number=" + proposal_number + ", prop_value=" + proposed_value + ", acc_value=" + lat.accepted_value + ", from=" + src + ", subset?=" + isSubset);
        if ( isSubset )
            onAckProposal( lat, proposal_number, proposed_value, src );
        else
            onNackProposal( lat, proposal_number, proposed_value, src );
    }

    private void onDecidedProposal( int round, Proposal decision, int proposal_number, Proposal proposed_value, int src )
    {
        boolean isSubset = proposed_value.containsAll( decision );
        Logger.log(service.id, "LATReceiver round=" + round, "onDecidedProposal: number=" + proposal_number + ", prop_value=" + proposed_value + ", decision=" + decision + ", from=" + src + ", subset?=" + isSubset);
        if ( isSubset )
            ((LATSender) sender).sendAck( round, proposal_number, src );
        else
        {
            Proposal accepted_value = new Proposal(decision);
            accepted_value.addAll( proposed_value );
            ((LATSender) sender).sendNack( round, proposal_number, src, accepted_value );
        }
    }
}
