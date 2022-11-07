package cs451.fifo;

import cs451.network.SocketService;
import cs451.urb.URBSender;

public class FIFOSender extends URBSender
{
    public FIFOSender( SocketService service )
    {
        super( service );
    }
}

