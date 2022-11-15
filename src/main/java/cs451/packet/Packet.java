package cs451.packet;

import cs451.Host;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Packet
{
    // TODO: extends from Message

    private final PacketTypes type;
    private final Host dest;
    private final int seqNr, origin, src, messages;

    public Packet( PacketTypes type, int seqNr, int origin, int src, Host dest, int messages )
    {
        this.type = type;
        this.seqNr = seqNr;
        this.origin = origin;
        this.src = src;
        this.dest = dest;
        this.messages = messages;
    }

    public Packet( Message msg, Host dest )
    {
        this( msg.type, msg.seq, msg.origin, msg.src, dest, msg.messages );
    }

    private static Packet inverseDirection( PacketTypes type, Packet packet )
    {
        int src = packet.getDestId();
        Host dest = Host.findById.get( packet.getSrc() );
        return new Packet( type, packet.getSeqNr(), packet.getOrigin(), src, dest, packet.getMessages() );
    }

    /**
     * Inverse direction and returns an ACK packet
     */
    public static Packet createACKPacket( Packet packet )
    {
        return inverseDirection( PacketTypes.ACK, packet );
    }

    /**
     * Inverse direction and returns a BRC packet
     */
    public static Packet createBRCPacket( Packet packet )
    {
        return inverseDirection( PacketTypes.BRC, packet );
    }

    public Packet( DatagramPacket from, Host dest )
    {
        ByteBuffer bb = ByteBuffer.wrap(from.getData());
        this.type = PacketTypes.parseType( bb.getChar() );
        this.seqNr = bb.getInt();
        this.origin = bb.getInt();
        this.src = bb.getInt();
        this.dest = dest;
        this.messages = bb.getInt();
    }

    public Packet withType( PacketTypes pt )
    {
        return new Packet( pt, seqNr, origin, src, dest, messages );
    }

    public Packet getRelay()
    {
        return new Packet( type, seqNr, origin, origin, dest, messages );
    }

    /**
     * converts the seqNr into the packet index
     */
    public int getIndex()
    {
        return seqNr / Message.MAX;
    }

    public DatagramPacket getDatagram()
    {
        // total max: 70
        ByteBuffer bb = ByteBuffer.allocate( 70 );
        bb.putChar( type.getTag() ); // 2 bytes
        bb.putInt( seqNr ); // 4 bytes
        bb.putInt( origin ); // 4 bytes
        bb.putInt( src ); // 4 bytes
        bb.putInt( messages ); // 4 bytes
        // messages * 4 bytes + messages - 1 (max 39)
        for ( int i = seqNr; i < seqNr + messages; i++ )
            bb.putInt( i );
        return new DatagramPacket( bb.array(), bb.capacity() );
    }
    public List<String> getFileLines() { return type.getFileLines( this ); }

    public PacketTypes getType() { return type; }
    public int getSeqNr() { return seqNr; }
    public int getOrigin() { return origin; }
    public int getSrc() { return src; }
    public int getDestId() { return dest.getId(); }
    public int getDestPort() { return dest.getPort(); }
    public InetAddress getDestAddress() { return dest.getAddress(); }
    public int getMessages() { return messages; }

    public Stream<Integer> getSeqRange()
    {
        return IntStream.range( seqNr, seqNr + messages )
            .boxed();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Packet that = (Packet) o;
        return
            type == that.type &&
            seqNr == that.seqNr &&
            src == that.src &&
            getDestId() == that.getDestId() &&
            messages == that.messages;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, seqNr, origin, src, dest, messages );
    }

    @Override
    public String toString()
    {
        return "TYPE=" + type +
            ", ORG=" + origin +
            ", SRC=" + src +
            ", DST=" + getDestId() +
            ", SEQ=" + seqNr +
            ", MSG=" + messages;
    }
}
