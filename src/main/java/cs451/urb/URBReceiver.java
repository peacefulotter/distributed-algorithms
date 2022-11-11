package cs451.urb;

import cs451.beb.BEBReceiver;
import cs451.network.SocketService;
import cs451.packet.Message;
import cs451.packet.Packet;
import cs451.utils.Logger;
import cs451.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class URBReceiver extends BEBReceiver
{
    // (origin, seq) -> List<p_i>
    private final ConcurrentMap<Pair<Integer, Integer>, List<Integer>> majority;
    private final ConcurrentLinkedQueue<Pair<Integer, Integer>> delivered;

    public URBReceiver( SocketService service )
    {
        super( service );
        this.majority = new ConcurrentHashMap<>();
        this.delivered = new ConcurrentLinkedQueue<>();
    }

    public void createMajority( Message m )
    {
        Pair<Integer, Integer> pair = Pair.fromMessage( m );
        List<Integer> processes = new ArrayList<>();
        processes.add( m.src );
        majority.put( pair, processes );
    }

    private boolean hasMajority( List<Integer> processes )
    {
        return processes.size() > Math.floor( service.getNbHosts() / 2f );
    }

    private void deliverRelay( Packet p )
    {
        Logger.log("URBReceiver", "Delivering relay " + p.getRelay() );
        deliver( p.getRelay() );
    }

    private void onRelay( Pair<Integer, Integer> p, Packet packet )
    {
        Integer src = packet.getSrc();
        System.out.println(majority);

        List<Integer> processes = majority.get( p );
        if ( hasMajority( processes ) || processes.contains( src ) )
            return;

        processes.add( src );
        majority.put( p, processes );

        if ( hasMajority( processes ) )
        {
            deliverRelay( packet );
            delivered.add( p );
            majority.remove( p );
        }
    }

    @Override
    public void onReceiveBroadcast( Packet packet )
    {
        Pair<Integer, Integer> pair = Pair.fromPacket( packet );
        System.out.println("Received " + pair);
        if ( majority.containsKey( pair ) )
            onRelay( pair, packet );
        else if ( !delivered.contains( pair ) )
        {
            createMajority( new Message( packet ) );
            ((URBSender) sender).relayBroadcast( packet );
        }
    }
}
