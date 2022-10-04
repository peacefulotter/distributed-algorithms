package cs451.parser.perfectlink;

import cs451.Host;
import cs451.parser.packet.PLPacket;

import java.io.IOException;
import java.net.DatagramPacket;

public class Sender extends Server
{
    private final int nbMessages;
    private int seqNr;

    public Sender( Host host, Host dest, String output, PLConfig config )
    {
        super( host, dest, output );
        seqNr = 1;
        this.nbMessages = config.getM();
        System.out.println("[SENDER] " + host);
    }

    private String broadcastPacket()
    {
        PLPacket packet = new PLPacket( 'b', seqNr++, host.getId() );
        boolean sent = sendPacket( packet );
        if ( !sent )
            seqNr--;        // retry?
        return packet.getMsg();
    }

    private String waitForAck()
    {
        DatagramPacket packet = getIncomingPacket();
        System.out.println(parsePacket( packet ));
        PLPacket delivery = new PLPacket( packet.getData() );
        System.out.println(host + "ACK: " + delivery.getMsg());
        return delivery.getMsg();
    }

    @Override
    protected boolean _run()
    {
        if ( seqNr < nbMessages )
        {
            // broadcast packet to receiver
            String bcMsg = broadcastPacket();
            // write broadcast msg
            handler.write( bcMsg );
            // wait for ack
            String ackMsg = waitForAck();

            // write deliver msg (ack) if not already delivered
            if ( !delivered.contains( ackMsg ) )
            {
                handler.write( ackMsg );
                delivered.add( ackMsg );
            }
        }
        else
        {
            System.out.println(host + " Done transmitting");
            return false;
        }

        return true;
    }
}
