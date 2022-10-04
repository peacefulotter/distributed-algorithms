package cs451.parser.perfectlink;

import cs451.Host;
import cs451.parser.packet.BroadcastPacket;
import cs451.parser.packet.DeliveryPacket;
import cs451.parser.packet.PLPacket;
import cs451.parser.packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;

public class Receiver extends Server
{
    public Receiver( Host host, Host dest, String output )
    {
        super( host, dest, output );
        System.out.println("[RECEIVER] " + host);
    }

    @Override
    protected boolean _run()
    {
        System.out.println( host + "host waiting" );

        DatagramPacket packet = getIncomingPacket();
        PLPacket bc = new PLPacket( packet.getData() );

        System.out.println( host + "Received from: " + packet.getPort() + ", msg: " + bc.getMsg() );
        System.out.println( parsePacket( packet ) );
        PLPacket deliverPacket = new PLPacket( 'd', bc.getSeqNr(), bc.getSender() );
        sendPacket( deliverPacket );

        return true;
    }
}
