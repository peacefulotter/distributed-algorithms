package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.packet.GroupedPacket;
import cs451.packet.PacketTypes;
import cs451.utils.Pair;
import cs451.utils.Sleeper;

import java.util.HashSet;
import java.util.Set;

import static cs451.utils.Logger.log;

public class PLReceiver extends SocketHandler
{
    // seq, src
    protected final Set<Pair<Integer, Integer>> delivered;

    protected PLSender sender;

    public PLReceiver( SocketService service )
    {
        super( service );
        this.delivered = new HashSet<>();
    }

    public void setSender( PLSender sender )
    {
        this.sender = sender;
    }


    public boolean deliver( GroupedPacket p )
    {
        Pair<Integer, Integer> pair = new Pair<>( p.seq, p.src );
        if ( delivered.contains( pair ) )
            return false;

        log( "Delivering : " + p );
        delivered.add( pair );
        return true;
    }

    public GroupedPacket getPacket()
    {
        GroupedPacket packet = service.getIncomingPacket();
        log( "Received : " + packet );
        return packet;
    }

    public void onPacket( GroupedPacket p )
    {
//        if (
//            p.type == PacketTypes.ACK ||
//            p.type == PacketTypes.LAT_ACK  ||
//            p.type == PacketTypes.LAT_NACK
//        )
//            sender.onAcknowledge( p );
//        else
//            handlePacket(p);
    }

    @Override
    public void run()
    {
        while ( !service.closed.get() )
        {
            GroupedPacket packet = getPacket();
            if ( packet != null && deliver(packet) )
                onPacket( packet );
            Sleeper.release();
        }
    }
}
