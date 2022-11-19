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
import java.util.concurrent.ConcurrentMap;

public class URBReceiver extends BEBReceiver
{
    // (origin, seq) -> List<p_i>
    private final ConcurrentMap<Pair<Integer, Integer>, List<Integer>> majority;
    private final List<Pair<Integer, Integer>> delivered;
    private final int threshold;

    public URBReceiver( SocketService service )
    {
        super( service );
        this.majority = new ConcurrentHashMap<>();
        this.delivered = new ArrayList<>();
        this.threshold = (int) Math.floor( service.getNbHosts() / 2f );
    }

    public void createEmptyMajority( Message m )
    {
        Pair<Integer, Integer> pair = Pair.fromMessage( m );
        majority.put( pair, new ArrayList<>() );
    }

    private void createMajority( Message m )
    {
        Pair<Integer, Integer> pair = Pair.fromMessage( m );
        List<Integer> processes = new ArrayList<>();
        processes.add( m.src );
        majority.put( pair, processes );
    }

    private boolean hasMajority( List<Integer> processes )
    {
        return processes.size() > threshold;
    }

    private void deliverRelay( Packet p )
    {
        Logger.log("URBReceiver", "Delivering relay " + p.getRelay() );
        deliver( p.getRelay() );
    }

    private void onRelay( Pair<Integer, Integer> p, Packet packet )
    {
        Integer src = packet.getSrc();

        List<Integer> processes = majority.get( p );
        if ( hasMajority( processes ) || processes.contains( src ) )
            return;

        processes.add( src );

        if ( hasMajority( processes ) )
        {
            deliverRelay( packet );
            delivered.add( p );
            majority.remove( p );
        }
        else
            majority.put( p, processes );
    }

    @Override
    public void onReceiveBroadcast( Packet packet )
    {
        Pair<Integer, Integer> pair = Pair.fromMessage( packet );
        System.out.println(delivered + " -- " + majority);
        if ( majority.containsKey( pair ) )
            onRelay( pair, packet );
        else if ( !delivered.contains( pair ) )
        {
            createMajority( packet );
            ((URBSender) sender).relayBroadcast( packet );
        }
    }
}
