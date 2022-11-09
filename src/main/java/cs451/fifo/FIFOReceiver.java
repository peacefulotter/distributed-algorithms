package cs451.fifo;

import cs451.network.SocketService;
import cs451.packet.Message;
import cs451.packet.Packet;
import cs451.urb.URBReceiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FIFOReceiver extends URBReceiver
{
    // host_id -> packets
    private final Map<Integer, List<Packet>> orderedPackets;

    public FIFOReceiver( SocketService service )
    {
        super( service );
        this.orderedPackets = new HashMap<>();
    }

    protected void onDeliver( Packet p )
    {
        List<Packet> packets = orderedPackets.get( p.getDestId() );
        if ( packets.contains( p ) )
            return;

        int last = packets.get( packets.size() - 1 ).getIndex();

        if ( last < p.getIndex() )
        {
            // add unknown packets until we reach the position where the packet is supposed to be put
            while ( last++ != p.getIndex() )
                packets.add( Packet.createUnknown() );
            packets.add( p );
        }
        else
        {
            // replace unknown packet for the real one
            packets.set( p.getIndex(), p );
        }
    }
}
