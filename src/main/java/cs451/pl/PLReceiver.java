package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.utils.Sleeper;

import java.util.HashSet;
import java.util.Set;

import static cs451.utils.Logger.log;

public class PLReceiver extends SocketHandler
{
    protected final Set<Integer> delivered;

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

    protected void sendAck( Packet packet )
    {
        Packet ack = Packet.createACKPacket( packet );
        log( "Sending ACK : " + ack );
        sendPacket( ack );
    }

    public void deliver( Packet packet )
    {
        int hc = packet.hashCode();
        if ( delivered.contains( hc ) )
            return;

        log( "Delivering : " + packet );
        delivered.add( hc );
        sender.onDeliver( packet );
        service.registerDeliver( packet );
    }

    public void onReceiveBroadcast( Packet packet )
    {
        deliver(packet);
    }

    public Packet getPacket()
    {
        Packet packet = service.getIncomingPacket();
        log( "Received : " + packet );
        return packet;
    }

    public void onPacket( Packet packet )
    {
        if ( packet.getType() == PacketTypes.BRC )
        {
            sendAck( packet );
            onReceiveBroadcast( packet );
        }
        else if ( packet.getType() == PacketTypes.ACK )
        {
            sender.onAcknowledge( packet );
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
