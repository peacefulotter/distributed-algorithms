package cs451.lat;

import cs451.Host;
import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.*;
import cs451.utils.Logger;

import java.util.Set;

public class LATSender extends BEBSender
{
    private final LATService lat;

    public LATSender( SocketService service, LATService lat )
    {
        super( service );
        this.lat = lat;
    }

    private void latBroadcast( SetMessage msg )
    {
        Logger.log(service.id, "LATSender", "Sending proposal: " + msg);
        Packet packet;
        for ( Host dest : service.getHosts() )
        {
            packet = new SetPacket( msg, dest );
            pp2pBroadcast( packet );
        }
    }

    /* upon propose(Set proposal) */
    @Override
    public SetMessage propose( Set<Integer> proposal )
    {
        Proposal p = new Proposal(proposal);
        SetMessage msg = getProposalMsg( p );
        lat.proposedValue = p;
        latBroadcast( msg );
        return msg;
    }

    private SetMessage getProposalMsg( Proposal proposal )
    {
        int apn = lat.active_proposal_number.incrementAndGet();
        lat.resetAcks();
        return new SetMessage(
            PacketTypes.LAT_PROP,
            apn,
            service.id,
            service.id,
            proposal
        );
    }

    public void sendProposal()
    {
        SetMessage msg = getProposalMsg( lat.proposedValue );
        Logger.log(service.id, "LATSender", "Sending new proposal: " + msg);
        addBroadcastQueue( msg );
    }

    public void moveNextProposal()
    {
        Logger.log(service.id, "LATSender","Moving to next proposal");
        proposalsToSend.incrementAndGet();
    }

    public void sendAck( int proposal_number, int src )
    {
        Logger.log(service.id, "LATSender","Sending ACK: prop_nb=" + proposal_number + " to=" + src);
        Packet p = new Packet(
            PacketTypes.LAT_ACK,
            proposal_number,
            service.id,
            service.id,
            src
        );
        addSendQueue( p );
    }

    public void sendNack( Set<Integer> accepted_value, int proposal_number, int src )
    {
        Logger.log(service.id, "LATSender", "Sending NACK: prop_nb=" + proposal_number + " to=" + src + " acc_value=" + accepted_value);
        SetPacket p = new SetPacket(
            PacketTypes.LAT_NACK,
            proposal_number,
            service.id,
            service.id,
            src,
            new Proposal(accepted_value) );
        addSendQueue( p );
    }
}
