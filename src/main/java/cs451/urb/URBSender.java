package cs451.urb;

import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.Message;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;

abstract public class URBSender extends BEBSender
{
    public URBSender( SocketService service )
    {
        super( service );
    }

    public void relayBroadcast( Packet packet )
    {
        Message msg = new Message( PacketTypes.BRC, packet.getRound(), packet.getPropNb(), service.id );
        // addBroadcastQueue( msg );
    }

    public void broadcast( Message msg )
    {
        ((URBReceiver) receiver).createEmptyMajority( msg );
        super.broadcast( msg );
    }
}

