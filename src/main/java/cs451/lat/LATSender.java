package cs451.lat;

import cs451.Host;
import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.*;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

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
        Packet packet;
        for ( Host dest : service.getHosts() )
        {
            packet = new SetPacket( msg, dest );
            pp2pBroadcast( packet );
        }
    }

    /* upon propose(Set proposal) */
    @Override
    public void propose( SetMessage msg )
    {
        lat.proposedValue = msg.proposal;
        latBroadcast( msg );
    }

    private SetMessage getProposalMsg()
    {
        int apn = lat.active_proposal_number.incrementAndGet();
        lat.resetAcks();
        return new SetMessage(
            PacketTypes.LAT_PROP,
            apn,
            service.id,
            service.id,
            lat.proposedValue
        );
    }

    public void sendProposal()
    {
        SetMessage msg = getProposalMsg();
        addBroadcastQueue( msg );
    }

    public void sendAck( int proposal_number, int src )
    {
        Packet p = new Packet(
            PacketTypes.LAT_ACK,
            proposal_number,
            service.id,
            service.id,
            src
        );
        addSendQueue( p );
    }

    public void sendNack( Proposal accepted_value, int proposal_number, int src )
    {
        SetPacket p = new SetPacket(
            PacketTypes.LAT_NACK,
            proposal_number,
            service.id,
            service.id,
            src,
            accepted_value );
        addSendQueue( p );
    }
}
