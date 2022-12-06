package cs451.lat;

import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.*;
import cs451.utils.Logger;

public class LATSender extends BEBSender
{
    public LATSender( SocketService service )
    {
        super( service );
    }

    /* upon propose(Set proposal) */
    @Override
    public SetMessage propose( int round, Proposal proposal )
    {
        LATService lat = ((LATReceiver) receiver).getLat( round );
        SetMessage msg = getProposalMsg( lat, proposal );
        lat.proposed_value = proposal;
        bebBroadcastSet( msg );
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
        Logger.log(service.id, "LATSender   round=" + lat.round, "Sending new proposal: " + msg);
        // TODO: don't broadcast to those who acked
        addBroadcastQueue( msg );
    }

    public void moveNextProposal()
    {
        Logger.log(service.id, "LATSender","Moving to next proposal");
        proposalsToSend.incrementAndGet();
    }

    public void sendAck( int round, int proposal_number, int src )
    {
        Packet p = new Packet(
            PacketTypes.LAT_ACK,
            round,
            proposal_number,
            service.id,
            src
        );
        Logger.log(service.id, "LATSender   round=" + round,"Sending ACK: " + p);
        addSendQueue( p );
    }

    public void sendNack( int round, int proposal_number, int src, Proposal accepted_value )
    {
        SetPacket p = new SetPacket(
            PacketTypes.LAT_NACK,
            round,
            proposal_number,
            service.id,
            src,
            accepted_value );
        Logger.log(service.id, "LATSender   round=" + round, "Sending NACK: " + p);
        addSendQueue( p );
    }
}
