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
            LATService lat = new LATService( service, c.getRound() );
            lat.proposed_value = c.getProposal();
            ((LATReceiver) receiver).addLat( lat );
        }
    }

    public void sendProposal( LATService lat, int apn )
    {
        Proposal prop = new Proposal(lat.proposed_value);
        PacketContent content = new PacketContent( PacketTypes.LAT_PROP, lat.round, apn, prop );
        Logger.log(service.id, "LATSender   round=" + lat.round, "Sending new proposal: " + content.string());
        addBroadcastQueue( content );
    }

    public void moveNextProposal()
    {
        Logger.log(service.id, "LATSender","Moving to next proposal");
        proposalsToSend.incrementAndGet();
    }
}
