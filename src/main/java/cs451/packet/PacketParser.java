package cs451.packet;

import cs451.beb.BEBSender;
import cs451.lat.Proposal;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PacketParser
{
    private static final int BASE_SIZE = 12;
    private static final int PER_CONTENT_SIZE = 10;
    private static final int INT_SIZE = 4;

    public static int maxBufSize( int ds )
    {
        return BASE_SIZE + BEBSender.MAX * ( (ds + 1) * INT_SIZE + PER_CONTENT_SIZE);
    }

    private static int capacity( List<PacketContent> contents )
    {
        int capacity = BASE_SIZE + PER_CONTENT_SIZE * contents.size();
        for ( PacketContent c : contents )
            capacity += (c.getProposal().size() + 1) * INT_SIZE;
        return capacity;
    }

    public static DatagramPacket format( GroupedPacket p )
    {
        int capacity = capacity( p.contents );
        ByteBuffer buffer = ByteBuffer.allocate( capacity );
        buffer.putInt( p.seq );
        buffer.putInt( p.src );
        buffer.putInt( p.contents.size() );
        for ( PacketContent c : p.contents )
        {
            buffer.putChar( c.getType().getTag() );
            buffer.putInt( c.getRound() );
            buffer.putInt( c.getProp_nb() );
            Proposal prop = c.getProposal();
            if ( prop.size() == 0 ) continue;
            buffer.putInt( prop.size() );
            for ( Integer i : prop )
                buffer.putInt(i);
        }
        return new DatagramPacket( buffer.array(), buffer.capacity() );
    }

    public static GroupedPacket parse( DatagramPacket dp, int dest )
    {
        ByteBuffer bb = ByteBuffer.wrap( dp.getData() );
        int seq = bb.getInt();
        int src = bb.getInt();
        int nb = bb.getInt();
        List<PacketContent> contents = new ArrayList<>(nb);
        for ( int i = 0; i < nb; i++ )
        {
            char t = bb.getChar();
            int round = bb.getInt();
            int prop_nb = bb.getInt();
            PacketTypes type = PacketTypes.parseType( t );
            if ( type == PacketTypes.LAT_ACK )
                contents.add( new PacketContent( type, round, prop_nb ) );
            else
            {
                int size = bb.getInt();
                Proposal prop = new Proposal(size);
                for ( int j = 0; j < size; j++ )
                    prop.add( bb.getInt() );
                contents.add( new PacketContent( type, round, prop_nb, prop ) );
            }
        }
        return new GroupedPacket( seq, src, contents, dest );
    }
}
