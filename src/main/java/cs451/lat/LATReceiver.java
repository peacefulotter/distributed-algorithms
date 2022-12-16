package cs451.lat;

import cs451.beb.BEBReceiver;
import cs451.network.SocketService;
import cs451.packet.GroupedPacket;
import cs451.packet.PacketContent;
import cs451.packet.PacketTypes;
import cs451.pl.PLSender;
import cs451.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LATReceiver extends BEBReceiver
{
    // round -> decision
    private final Map<Integer, Proposal> decided;
    // round -> service (concurrency verified)
    private final ConcurrentHashMap<Integer, LATService> latServices;
    // round -> nb of proposals
    private final Map<Integer, Set<Integer>> receivedProposals;

    // round -> proposal (accepted_values)
    private final Map<Integer, Proposal> accepted_value;

    private LATSender sender;

    public LATReceiver( SocketService service )
    {
        super( service );
        this.decided = new HashMap<>();
        this.latServices = new ConcurrentHashMap<>();
        this.receivedProposals = new HashMap<>();
        this.accepted_value = new HashMap<>();
    }

    @Override
    public void setSender( PLSender sender )
    {
        super.setSender( sender );
        this.sender = (LATSender) sender;
    }

    public void setDecided( int round, Proposal decision )
    {
        this.decided.put( round, decision );
        this.receivedProposals.remove( round );
        this.latServices.remove( round );
        this.accepted_value.remove( round );
    }

    public void addLat(LATService lat)
    {
        latServices.put( lat.round, lat );
    }

    private PacketContent onPacketContent( int src, PacketContent c )
    {
        int round = c.getRound();
        int prop_nb = c.getProp_nb();
        PacketTypes t = c.getType();
        Proposal prop = c.getProposal();
        LATService lat = latServices.get( round );
        Logger.log(service.id, "LATReceiver round=" + round, c.string());

        // Round already decided and receiving a LAT_PROP
        if (
            lat == null &&
            decided.containsKey( round ) &&
            c.getType() == PacketTypes.LAT_PROP
        )
        {
            Proposal decision = decided.get( round );
            return onDecidedProposal( round, decision, prop_nb, prop );
        }
        // Round not decided yet -> participate to agreement
        else if ( t == PacketTypes.LAT_PROP )
        {
            PacketContent res = tryEarlyDecide( lat, src, prop_nb, prop );
            if ( res == null )
                return onProposal( round, prop_nb, prop );
            return res;
        }
        else if ( lat != null )
        {
            if ( t == PacketTypes.LAT_NACK )
                onLatNack( lat, prop_nb, prop );
            else if ( t == PacketTypes.LAT_ACK )
                onLatAck( lat, prop_nb );
        }
        return null;
    }

    // Receive N proposals from different src -> decide early
    private PacketContent tryEarlyDecide(LATService lat, int src, int prop_nb, Proposal proposed_value )
    {
        if ( lat == null ) return null;

        int r = lat.round;
        if ( canEarlyDecide(src, r) )
        {
            Proposal decision = accepted_value.get( r );
            Logger.log(service.id, "LATReceiver round=" + r, "#### Deciding early.., old accepted: " + decision);
            decision.addAll( proposed_value );
            Logger.log(service.id, "LATReceiver round=" + r, "Deciding early.., new accepted: " + decision);
            lat.decide(sender, this, decision);
            return onDecidedProposal( r, decision, prop_nb, proposed_value );
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
            PacketContent r = onPacketContent( p.src, c );
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

        lat.onAck( sender, this );
    }

    /* upon reception of <NACK, proposal_number, value>
     *  s.t. proposal_number == active_proposal_number */
    public void onLatNack( LATService lat, int proposal_number, Proposal value )
    {
        int apn = lat.active_proposal_number.get();
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on NACK, prop_nb=" + proposal_number + ", active_prop_nb=" + apn );
        if ( proposal_number != apn )
            return;

        lat.onNack( sender, value );
        Logger.log(service.id, "LATReceiver round=" + lat.round, "on NACK Proposal: new acc_value=" + accepted_value );
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
    private PacketContent onAckProposal( int round, int proposal_number, Proposal proposed_value )
    {
        accepted_value.put( round, proposed_value );
        Logger.log(service.id, "LATReceiver round=" + round, "on ACK Proposal: new acc_value=" + proposed_value );
        return getAck( round, proposal_number );
    }

    private PacketContent onNackProposal( int round, int proposal_number, Proposal proposed_value )
    {
        Proposal accepted = accepted_value.get( round );
        accepted.addAll( proposed_value );
        accepted_value.put( round, accepted );
        return getNack( round, proposal_number, accepted );
    }

    private boolean canEarlyDecide( int src, int round)
    {
        Set<Integer> proc = receivedProposals.getOrDefault(round, new HashSet<>());
        proc.add( src );
        receivedProposals.put( round, proc );
        Logger.log(service.id, "LATReceiver round=" + round, "checkReceiveProposals " + proc + " / " + service.getNbHosts() );
        return proc.size() >= service.getNbHosts();
    }


    private PacketContent onProposal(int round, int proposal_number, Proposal proposed_value )
    {
        // Normal onProposal
        Proposal accepted = accepted_value.getOrDefault( round, new Proposal() );
        boolean isSubset = proposed_value.containsAll( accepted );
        Logger.log(service.id, "LATReceiver round=" + round, "onProposal: prop_nb=" + proposal_number + ", prop_value=" + proposed_value + ", acc_value=" + accepted + ", subset?=" + isSubset);
        if ( isSubset )
            return onAckProposal( round, proposal_number, proposed_value );
        else
            return onNackProposal( round, proposal_number, proposed_value );
    }

    private PacketContent onDecidedProposal( int round, Proposal decision, int proposal_number, Proposal proposed_value )
    {
        boolean isSubset = proposed_value.containsAll( decision );
        Logger.log(service.id, "LATReceiver round=" + round, "onDecidedProposal: number=" + proposal_number + ", prop_value=" + proposed_value + ", decision=" + decision  + ", subset?=" + isSubset);
        if ( isSubset )
            return getAck( round, proposal_number );
        else
        {
            Proposal accepted = new Proposal(decision);
            accepted.addAll( proposed_value );
            return getNack( round, proposal_number, accepted );
        }
    }
}
