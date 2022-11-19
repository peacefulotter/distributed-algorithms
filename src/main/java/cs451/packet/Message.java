package cs451.packet;

import cs451.network.SocketService;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Message implements Comparable<Packet>
{
    public static final int MAX = 8;

    public final PacketTypes type;
    public final int seq, origin, src, messages;

    public Message( PacketTypes type, int seq, int origin, int src, int messages )
    {
        this.type = type;
        this.seq = seq;
        this.origin = origin;
        this.src = src;
        this.messages = messages;
    }

    public Message( Message m )
    {
        this( m.type, m.seq, m.origin, m.src, m.messages );
    }

    public Message( DatagramPacket from )
    {
        ByteBuffer bb = ByteBuffer.wrap(from.getData());
        this.type = PacketTypes.parseType( bb.getChar() );
        this.seq = bb.getInt();
        this.origin = bb.getInt();
        this.src = bb.getInt();
        this.messages = bb.getInt();
    }

    protected Comparator<Message> getComparator()
    {
        return Comparator
            .comparing( Message::getType )
            .thenComparing( Message::getOrigin )
            .thenComparing( Message::getSrc )
            .thenComparing( Message::getSeqNr );
    }

    /**
     * converts the seqNr into the packet index
     */
    public int getIndex()
    {
        return seq / MAX;
    }

    public DatagramPacket getDatagram()
    {
        // total max: 70
        ByteBuffer bb = ByteBuffer.allocate( 70 );
        bb.putChar( type.getTag() ); // 2 bytes
        bb.putInt( seq ); // 4 bytes
        bb.putInt( origin ); // 4 bytes
        bb.putInt( src ); // 4 bytes
        bb.putInt( messages ); // 4 bytes
        // messages * 4 bytes + messages - 1 (max 39)
        for ( int i = seq; i < seq + messages; i++ )
            bb.putInt( i );
        return new DatagramPacket( bb.array(), bb.capacity() );
    }

    public List<String> getFileLines() { return type.getFileLines( this ); }

    public Stream<Integer> getSeqRange()
    {
        return IntStream.range( seq, seq + messages )
            .boxed();
    }

    public static Message getFirst( SocketService service )
    {
        int messages = Math.min( service.nbMessages, MAX );
        return new Message( PacketTypes.BRC, 1, service.id, service.id, messages );
    }

    public Message getNext( SocketService service )
    {
        int nextSeq = seq + MAX;
        int nextMsg = Math.min( service.nbMessages - nextSeq + 1, MAX );
        return new Message( PacketTypes.BRC, nextSeq, service.id, service.id, nextMsg );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Message m = (Message) o;
        return seq == m.seq && origin == m.origin && src == m.src && type == m.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, seq, origin, src );
    }

    @Override
    public String toString()
    {
        return "TYPE=" + type +
            ", SEQ=" + seq +
            ", ORG=" + origin +
            ", SRC=" + src +
            ", MSG=" + messages;
    }

    public PacketTypes getType() { return type; }
    public int getSeqNr() { return seq; }
    public int getOrigin() { return origin; }
    public int getSrc() { return src; }
    public int getMessages() { return messages; }

    @Override
    public int compareTo( Packet o )
    {
        return getComparator().compare( this, o );
    }
}
