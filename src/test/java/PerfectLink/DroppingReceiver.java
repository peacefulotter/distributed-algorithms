package PerfectLink;

import cs451.Host;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.perfectlink.Receiver;

import java.net.DatagramPacket;

public class DroppingReceiver extends Receiver
{
    private static final double DROPPING_RATE = 0.25d;

    public DroppingReceiver( Host host, String output )
    {
        super( host, output );
    }

    @Override
    protected boolean runCallback()
    {
        System.out.println( host + "waiting..." );

        DatagramPacket packet = getIncomingPacket();
        Packet bc = new Packet( PacketTypes.DELIVER, packet );

        System.out.println( host + "Received from: " + packet.getPort() + ", msg: " + bc.getMsg() );

        if ( Math.random() <= DROPPING_RATE )
        {
            System.out.println("Voluntarily dropping packet");
            return true;
        }

        sendAck( packet, bc );

        if ( !delivered.contains( bc.getMsg() ) )
        {
            handler.register( bc );
            delivered.add( bc.getMsg() );
        }

        return true;
    }
}
