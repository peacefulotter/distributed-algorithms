package cs451.fifo;

import cs451.network.SeqMsg;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.urb.URBReceiver;

public class FIFOReceiver extends URBReceiver
{
    private SeqMsg cur;

    public FIFOReceiver( SocketService service )
    {
        super( service );
        this.cur = SeqMsg.getFirst( service.nbMessages );
    }

    // TODO:
    protected void onDeliver( Packet packet )
    {
        SeqMsg sm = new SeqMsg( packet );
        if ( cur.equals( sm ) )
        {
            deliver();
            cur = cur.getNext( service.nbMessages );
        }
    }
}
