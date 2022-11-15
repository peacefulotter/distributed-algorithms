package cs451.urb;

import cs451.beb.BEBSender;
import cs451.network.SocketService;
import cs451.packet.Message;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;

public class URBSender extends BEBSender
{
    public URBSender( SocketService service )
    {
        super( service );
    }

    public void relayBroadcast( Packet packet )
    {
        Message msg = new Message( PacketTypes.BRC, packet.getSeqNr(), packet.getOrigin(), service.id, packet.getMessages() );
        addBroadcastQueue( msg );
    }

    @Override
    public void broadcast( Message msg )
    {
        ((URBReceiver) receiver).createMajority( msg );
        bebBroadcast( msg );
    }
}

