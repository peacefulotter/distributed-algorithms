package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.utils.Sleeper;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import static cs451.utils.Logger.log;

public class PLReceiver extends SocketHandler
{
    // protected final ConcurrentMap<Packet, Integer> packetTimeouts;
    protected final List<Packet> delivered;

    protected PLSender sender;

    public PLReceiver( SocketService service )
    {
        super( service );
        // this.packetTimeouts = new ConcurrentHashMap<>();
        this.delivered = new ArrayList<>();
    }

    public void setSender( PLSender sender )
    {
        this.sender = sender;
    }

    /*public void addPacketTimeout( Packet packet )
    {
        this.packetTimeouts.put( packet, service.timeout.get() );
    }
*/
    protected void sendAck( Packet packet )
    {
        Packet ack = Packet.createACKPacket( packet );
        log( "Sending ACK : " + ack );
        sendPacket( ack );
    }

    public void deliver( Packet packet )
    {
        if ( delivered.contains( packet ) )
            return;

        log( "Delivering : " + packet );
        delivered.add( packet );
        service.registerAck( packet );
        service.timeout.decrease();
    }

    // TODO: delete?
    /**
     * Update the Hashmap of timeouts
     * and retransmit the packets that waited for more than their TO
     */
    /*private void updateTimeouts()
    {
        // update
        packetTimeouts.replaceAll( (p, to) ->
            to - Math.round( Timeout.time() )
        );
        // retransmit
        packetTimeouts.forEach( (p, to) -> {
            if ( to <= 0 ) sender.pp2pBroadcast( p );
        } );
        packetTimeouts.entrySet()
            .removeIf( (e) -> e.getValue() <= 0 );
    }*/

    public Packet getPacket()
    {
        // service.setTimeout();
        Packet packet = service.getIncomingPacket();
        // updateTimeouts();
//        if ( dp == null )
//        {
//            service.timeout.increase();
//            return null;
//        }
        log( "Received : " + packet );
        return packet;
    }

    public void onPacket( Packet packet )
    {
        if ( packet.getType() == PacketTypes.BROADCAST )
        {
            // packetTimeouts.remove( packet );
            sendAck( packet );
            deliver( packet );
        }
        else if ( packet.getType() == PacketTypes.ACK )
        {
            sender.onAck( packet );
        }
    }

    @Override
    public void run()
    {
        while ( !service.closed.get() )
        {
            Packet packet = getPacket();
            if ( packet != null )
                onPacket( packet );
            Sleeper.release();
        }
    }
}