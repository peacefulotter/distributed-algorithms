package PerfectLink;

import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.parser.ParserResult;
import cs451.perfectlink.Receiver;

import java.net.DatagramPacket;

public class DroppingReceiver extends Receiver
{
    private static final double DROPPING_RATE = 0.25d;

    public DroppingReceiver( ParserResult result )
    {
        super( result.host, result.output );
    }

    @Override
    protected boolean runCallback()
    {
        DatagramPacket packet = getIncomingPacket();
        Packet bc = new Packet( PacketTypes.DELIVER, packet );

        log( "Received from: " + packet.getPort() + ", msg: " + bc.getMsg() );

        if ( Math.random() <= DROPPING_RATE )
        {
            log("Voluntarily dropping packet");
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
