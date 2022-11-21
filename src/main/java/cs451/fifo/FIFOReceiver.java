package cs451.fifo;

import cs451.Host;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.pl.PLReceiver;
import cs451.urb.URBReceiver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FIFOReceiver extends URBReceiver
{
    // host_id -> last ordered seq delivered
    private final Map<Integer, OrderPreserver> hostOrders;

    public FIFOReceiver( SocketService service )
    {
        super( service );
        this.hostOrders = new HashMap<>();
        Host.findById.keySet().forEach( id ->
            hostOrders.put( id, new OrderPreserver() )
        );
    }

    private void resolvePending( OrderPreserver preserver )
    {
        if ( preserver.pending.isEmpty() )
            return;

        Packet cur;
        int last = preserver.lastResolved;
        while ( (cur = preserver.pending.get( last )) != null )
        {
            super.deliver( cur );
            preserver.pending.remove( last );
            last++;
        }

        preserver.lastResolved = last;
    }

    /**
     * When URBReceiver delivers, FIFOReceiver catches the delivery
     * and ensures the in-order delivery.
     * i.e. Calls PLReceiver.deliver() in seq order
     * - Given packet comes from Packet.getRelay() => origin == src
     */
    @Override
    public void deliver( Packet p )
    {
        OrderPreserver preserver = hostOrders.get( p.getSrc() );
        int index = p.getIndex();

        // Packet is received in order
        if ( index == preserver.lastResolved )
        {
            super.deliver( p ); // call PL.deliver
            preserver.lastResolved++;
            resolvePending( preserver );
        }
        // Packet is delivered before the others -> add to pending
        else if ( index > preserver.lastResolved )
            preserver.pending.put( index, p );
    }
}
