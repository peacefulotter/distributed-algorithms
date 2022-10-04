package cs451.parser.packet;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class DeliveryPacket extends Packet
{
    private final int sender;

    public DeliveryPacket( int seqNr, int sender )
    {
        super( 'b', seqNr );
        this.sender = sender;
    }

    public DeliveryPacket( byte[] bytes )
    {
        super( bytes, 10 );
        ByteBuffer wrapped = ByteBuffer.wrap( bytes ); // big-endian by default
        this.sender = wrapped.getInt( 4 );
    }

    @Override
    public String getMsg()
    {
        return tag + " " + sender + " " + this.seqNr;
    }

    public int getSender()
    {
        return sender;
    }

    @Override
    public String toString()
    {
        return super.toString() + ", sender: " + sender;
    }
}
