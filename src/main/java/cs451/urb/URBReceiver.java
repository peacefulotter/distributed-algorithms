package cs451.urb;

import cs451.beb.BEBReceiver;
import cs451.network.SeqMsg;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.pl.PLReceiver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class URBReceiver extends BEBReceiver
{

    public URBReceiver( SocketService service )
    {
        super( service );
    }
}
