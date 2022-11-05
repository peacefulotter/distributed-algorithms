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

    // if ( acknowledged.incrementAndGet() >= hosts.size() / 2 )
    //      done = true;
}
