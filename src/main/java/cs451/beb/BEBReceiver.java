package cs451.beb;

import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.pl.PLReceiver;

public class BEBReceiver extends PLReceiver
{
    public BEBReceiver( SocketService service )
    {
        super( service );
    }

    @Override
    public void onPacket( Packet packet )
    {
        // if ( acknowledged.incrementAndGet() >= hosts.size() / 2 )
        //      done = true;

        // received ACK => send next packet
        if ( packet.getType() == PacketTypes.ACK )
        {
            packetTimeouts.remove( packet );
            sender.setPacketsToSent( 1 );
        }
    }
}
