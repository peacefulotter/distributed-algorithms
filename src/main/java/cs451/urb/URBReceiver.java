package cs451.urb;

import cs451.beb.BEBReceiver;
import cs451.network.SocketService;
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

    public URBReceiver( SocketService service )
    {
        super( service );
        this.majority = new ConcurrentHashMap<>();
    }

    public void createMajority( Integer origin, Integer seq )
    {
        Pair<Integer, Integer> pair = new Pair<>( origin, seq );
        List<Integer> seqMaj = new ArrayList<>();
        seqMaj.add( service.id );
        majority.put( pair, seqMaj );
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

        List<Integer> processes = majority.get( p );
        if ( processes.contains( src ) )
            return;
        else if ( hasMajority( processes ) )
            deliverRelay( packet );

        processes.add( src );
        majority.put( p, processes );

        if ( hasMajority( processes ) )
            deliverRelay( packet );
    }

    @Override
    public void onReceiveBroadcast( Packet packet )
    {
        Pair<Integer, Integer> p = Pair.fromPacket( packet );
        if ( majority.containsKey( p ) )
            onRelay( p, packet );
        else
        {
            createMajority( packet.getOrigin(), packet.getSeqNr() );
            ((URBSender) sender).relayBroadcast( packet );
        }
    }
}
