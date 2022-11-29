package cs451.lat;

import cs451.Host;
import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.*;
import cs451.utils.Logger;

import java.util.Set;

public class LATSender extends BEBSender
{
    public LATSender( SocketService service )
    {
        super( service );
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
    public SetMessage propose( int round, Proposal proposal )
    {
        LATService lat = ((LATReceiver) receiver).getLat( round );
        SetMessage msg = getProposalMsg( lat, proposal );
        lat.proposed_value = proposal;
        latBroadcast( msg );
        return msg;
    }

    private SetMessage getProposalMsg( LATService lat, Proposal proposal )
    {
        int apn = lat.active_proposal_number.incrementAndGet();
        lat.resetAcks();
        return new SetMessage(
            PacketTypes.LAT_PROP,
            lat.round,
            apn,
            service.id,
            proposal
        );
    }

    public void sendProposal( LATService lat )
    {
        SetMessage msg = getProposalMsg( lat, lat.proposed_value );
        Logger.log(service.id, "LATSender", "Sending new proposal: " + msg);
        addBroadcastQueue( msg );
    }

    public void moveNextProposal()
    {
        Logger.log(service.id, "LATSender","Moving to next proposal");
        proposalsToSend.incrementAndGet();
    }

    public void sendAck( int round, int proposal_number, int src )
    {
        Logger.log(service.id, "LATSender","Sending ACK: prop_nb=" + proposal_number + " to=" + src);
        Packet p = new Packet(
            PacketTypes.LAT_ACK,
            round,
            proposal_number,
            service.id,
            src
        );
        addSendQueue( p );
    }

    public void sendNack( int round, int proposal_number, int src, Proposal accepted_value )
    {
        Logger.log(service.id, "LATSender", "Sending NACK: prop_nb=" + proposal_number + " to=" + src + " acc_value=" + accepted_value);
        SetPacket p = new SetPacket(
            PacketTypes.LAT_NACK,
            round,
            proposal_number,
            service.id,
            src,
            accepted_value );
        addSendQueue( p );
    }
}
