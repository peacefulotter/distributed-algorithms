package cs451.perfectlink;

import cs451.Host;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

public class Receiver extends Server
{
    protected final List<String> delivered;

    public Receiver( Host host, String output )
    {
        super( host, output );
        this.delivered = new ArrayList<>();
    }

    protected void sendAck( DatagramPacket packet, Packet broadcast )
    {
        Packet ack = new Packet( PacketTypes.ACK, broadcast.getSeqNr(), host.getId() );
        log( "Sending ACK to: " + packet.getPort() + ", msg: " + ack.getMsg() );
        try
        {
            sendPacket( ack, ( dg ) -> {
                dg.setAddress( packet.getAddress() );
                dg.setPort( packet.getPort() );
            } );
        } catch ( IOException e )
        {
            terminate( e );
        }
    }

    @Override
    protected boolean runCallback()
    {
        DatagramPacket packet = getIncomingPacket();
        Packet bc = new Packet( PacketTypes.DELIVER, packet );

        log( "Received from: " + packet.getPort() + ", msg: " + bc.getMsg() );

        sendAck( packet, bc );

        if ( !delivered.contains( bc.getMsg() ) )
        {
            handler.register( bc );
            delivered.add( bc.getMsg() );
        }

        return true;
    }
}
