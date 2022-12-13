package cs451.lat;

import cs451.beb.BEBReceiver;
import cs451.network.SocketService;
import cs451.packet.GroupedPacket;
import cs451.packet.PacketContent;
import cs451.packet.PacketTypes;
import cs451.utils.Logger;

import java.util.*;
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


    // delivered cleanup
    // nack == N

    private PacketContent onPacketContent( PacketContent c )
    {
        int round = c.getRound();
        Logger.log(service.id, "LATReceiver round=" + round, c.string());

        LATService lat = getLat( round );
        // Round already decided and receiving a LAT_PROP
        if (
            lat == null &&
            decided.containsKey( round ) &&
            c.getType() == PacketTypes.LAT_PROP
        )
        {
            Proposal decision = decided.get( round );
            return onDecidedProposal( round, decision, c.getProp_nb(), c.getProposal() );
        }
        // Round not decided yet -> participate to agreement
        else if ( lat != null )
        {
            PacketTypes t = c.getType();
            if ( t == PacketTypes.LAT_PROP )
                return onProposal( lat, c.getProp_nb(), c.getProposal() );
            else if ( t == PacketTypes.LAT_NACK )
                onLatNack( lat, c.getProp_nb(), c.getProposal() );
            else if ( t == PacketTypes.LAT_ACK )
                onLatAck( lat, c.getProp_nb() );
        }
        return null;
    }

    public void onPacket( GroupedPacket p )
    {
        Logger.log(service.id, "LATReceiver", p);
        int size = p.contents.size();

        // Treat each content in the packet
        List<PacketContent> res = new ArrayList<>(size);
        for (PacketContent c : p.contents)
        {
            PacketContent r = onPacketContent( c );
            if ( r != null )
                res.add( r );
        }

        Logger.log(service.id, "LATReceiver", "res to SendQueue: " + res);

        if ( res.size() > 0 )
            sender.addResponseQueue( res, p.src );
    }

    /* upon reception of <ACK, proposal_number>
     *  s.t. proposal_number == active_proposal_number */
    public void onLatAck( LATService lat, int proposal_number )
    {
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on ACK, prop_nb=" + proposal_number + ", active_prop_nb=" + lat.active_proposal_number.get() );
        if ( proposal_number != lat.active_proposal_number.get() )
            return;

        lat.onAck((LATSender) sender, this );
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

    private PacketContent getAck( int round, int proposal_number )
    {
        return new PacketContent( PacketTypes.LAT_ACK, round, proposal_number );
    }

    private PacketContent getNack( int round, int proposal_number, Proposal accepted_value )
    {
        return new PacketContent( PacketTypes.LAT_NACK, round, proposal_number, accepted_value );
    }

    // acceptor
    private PacketContent onAckProposal( LATService lat, int proposal_number, Proposal proposed_value )
    {
        lat.accepted_value = proposed_value;
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on ACK Proposal: new acc_value=" + lat.accepted_value );
        return getAck( lat.round, proposal_number );
    }

    private PacketContent onNackProposal( LATService lat, int proposal_number, Proposal proposed_value )
    {
        lat.accepted_value.addAll( proposed_value );
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on NACK Proposal: new acc_value=" + lat.accepted_value );
        return getNack( lat.round, proposal_number, lat.accepted_value );
    }


    // TODO: for performance tests:
    // TODO: if receive N proposal -> decide
    private PacketContent onProposal( LATService lat, int proposal_number, Proposal proposed_value )
    {
        boolean isSubset = proposed_value.containsAll( lat.accepted_value );
        Logger.log(service.id, "LATReceiver round=" + lat.round, "onProposal: number=" + proposal_number + ", prop_value=" + proposed_value + ", acc_value=" + lat.accepted_value + ", subset?=" + isSubset);
        if ( isSubset )
            return onAckProposal( lat, proposal_number, proposed_value );
        else
            return onNackProposal( lat, proposal_number, proposed_value );
    }

    private PacketContent onDecidedProposal( int round, Proposal decision, int proposal_number, Proposal proposed_value )
    {
        boolean isSubset = proposed_value.containsAll( decision );
        Logger.log(service.id, "LATReceiver round=" + round, "onDecidedProposal: number=" + proposal_number + ", prop_value=" + proposed_value + ", decision=" + decision  + ", subset?=" + isSubset);
        if ( isSubset )
            return getAck( round, proposal_number );
        else
        {
            Proposal accepted_value = new Proposal(decision);
            accepted_value.addAll( proposed_value );
            return getNack( round, proposal_number, accepted_value );
        }
    }
}
