package cs451.parser.perfectlink;

import cs451.Host;
import cs451.parser.packet.Packet;
import cs451.parser.packet.PacketTypes;

import java.io.IOException;
import java.net.DatagramPacket;

public class Receiver extends Server
{
    public Receiver( Host host, String output )
    {
        super( host, output );
    }

    private void sendAck( DatagramPacket packet, Packet broadcast )
    {
        Packet ack = new Packet( PacketTypes.ACK, broadcast.getSeqNr(), host.getId() );
        System.out.println( host + "Sending ACK to: " + packet.getPort() + ", msg: " + ack.getMsg() );
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
        System.out.println( host + "waiting..." );

        DatagramPacket packet = getIncomingPacket();
        Packet bc = new Packet( PacketTypes.DELIVER, packet );

        System.out.println( host + "Received from: " + packet.getPort() + ", msg: " + bc.getMsg() );

        sendAck( packet, bc );

        if ( !delivered.contains( bc.getMsg() ) )
        {
            handler.register( bc );
            delivered.add( bc.getMsg() );
        }

        return true;
    }
}
