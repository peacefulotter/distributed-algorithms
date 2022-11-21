package cs451.beb;

import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.pl.PLReceiver;

public class BEBReceiver extends PLReceiver
{
    public BEBReceiver( SocketService service )
    {
        super( service );
    }

    @Override
    public void onReceiveBroadcast( Packet packet )
    {
    }
}
