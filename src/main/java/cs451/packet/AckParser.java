package cs451.packet;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class AckParser
{
    public static final byte ACK_TAG = (byte) PacketClass.ACK.ordinal();
    private static final int BASE_SIZE = 9;

    public static DatagramPacket format( GroupedPacket p )
    {
        ByteBuffer buffer = ByteBuffer.allocate( BASE_SIZE );
        buffer.put( ACK_TAG );
        buffer.putInt( p.seq );
        buffer.putInt( p.src );
        return new DatagramPacket( buffer.array(), buffer.capacity() );
    }

    public static MiniPacket parse( DatagramPacket dp, int dest )
    {
        ByteBuffer bb = ByteBuffer.wrap( dp.getData() );
        int seq = bb.getInt();
        int src = bb.getInt();
        return new MiniPacket( seq, src, dest );
    }
}
