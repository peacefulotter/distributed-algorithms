package cs451.packet;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class AckParser
{
    public static final byte ACK_TAG = (byte) PacketClass.ACK.ordinal();
    private static final int BASE_SIZE = 13;

    public static DatagramPacket format( GroupedPacket p )
    {
        ByteBuffer buffer = ByteBuffer.allocate( BASE_SIZE );
        buffer.put( ACK_TAG );
        buffer.putInt( p.seq );
        buffer.putInt( p.src );
        buffer.putInt( p.dest );
        return new DatagramPacket( buffer.array(), buffer.capacity() );
    }

    public static MiniPacket parse( DatagramPacket dp )
    {
        ByteBuffer bb = ByteBuffer.wrap( dp.getData() );
        bb.get(); // remove first byte = tag
        int seq = bb.getInt();
        int src = bb.getInt();
        int dest = bb.getInt();
        return new MiniPacket( seq, src, dest );
    }
}
