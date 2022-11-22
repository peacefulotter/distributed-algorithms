package cs451.lat;

import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.*;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class LATSender extends BEBSender
{
    private boolean status = false;

    private final LATService lat;

    public LATSender( SocketService service, LATService lat )
    {
        super( service );
        this.lat = lat;
    }

    // TODO: this should override broadcast
    /* upon propose(Set proposal) */
    public void propose( Proposal proposal )
    {
        lat.proposedValue = new Proposal( proposal );
        status = true;
        sendProposal();
    }

    public void sendProposal()
    {
        int apn = lat.active_proposal_number.incrementAndGet();
        SetMessage msg = new SetMessage(
            PacketTypes.LAT_PROP,
            apn,
            service.id,
            service.id,
            lat.proposedValue
        );
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
