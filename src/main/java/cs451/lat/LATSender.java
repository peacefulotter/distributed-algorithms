package cs451.lat;

import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.*;
import cs451.utils.Logger;

import java.util.List;

public class LATSender extends BEBSender
{
    public LATSender( SocketService service )
    {
        super( service );
    }

    /* upon propose(Set proposal) */
    @Override
    public void onPropose( List<PacketContent> contents )
    {
        for ( PacketContent c : contents )
        {
            LATService lat = ((LATReceiver) receiver).getLat( c.getRound() );
            lat.proposed_value = c.getProposal();
        }
    }

    public void sendProposal( LATService lat )
    {
        int apn = lat.active_proposal_number.incrementAndGet();
        lat.resetAcks();
        PacketContent content = new PacketContent( PacketTypes.LAT_PROP, lat.round, apn, lat.proposed_value );
        Logger.log(service.id, "LATSender   round=" + lat.round, "Sending new proposal: " + content);
        // TODO: don't broadcast to those who acked
        addBroadcastQueue( content );
    }

    public void moveNextProposal()
    {
        Logger.log(service.id, "LATSender","Moving to next proposal");
        proposalsToSend.incrementAndGet();
    }
}
