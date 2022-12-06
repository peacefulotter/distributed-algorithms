package cs451.packet;

import cs451.Host;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Objects;

public class Packet extends Message
{
    public static final int BUFFER_CAPACITY = 14;

    public final Host dest;

    public Packet( PacketTypes type, int round, int prop_nb, int src, Host dest )
    {
        super(type, round, prop_nb, src );
        this.dest = dest;
    }

    public Packet( PacketTypes type, int round, int prop_nb, int src, int dest )
    {
        this( type, round, prop_nb, src, Host.findById.get(dest) );
    }

    public Packet( Message msg, Host dest )
    {
        super( msg );
        this.dest = dest;
    }

    public static Packet fromDatagram( ByteBuffer bb, PacketTypes type, DatagramPacket from, Host dest )
    {
        return new Packet(
            type,
            bb.getInt(),
            bb.getInt(),
            bb.getInt(),
            dest
        );
    }

    public static ByteBuffer getBuffer( DatagramPacket dp )
    {
        return ByteBuffer.wrap(dp.getData());
    }

    private static Packet inverse( PacketTypes type, Packet packet )
    {
        int src = packet.getDestId();
        Host dest = Host.findById.get( packet.getSrc() );
        return new Packet( type, packet.getRound(), packet.getPropNb(), src, dest );
    }

    /**
     * Inverse direction and returns an ACK packet
     */
    public static Packet createACKPacket( Packet packet )
    {
        return inverse( PacketTypes.ACK, packet );
    }

    /**
     * Inverse direction and returns an BRC packet
     */
    public static Packet createBRCPacket( Packet packet )
    {
        return inverse( PacketTypes.BRC, packet );
    }

    public Packet withType( PacketTypes pt )
    {
        return new Packet( pt, round, prop_nb, src, dest );
    }

    protected ByteBuffer getPacketBuffer( int capacity )
    {
        ByteBuffer buffer = ByteBuffer.allocate( capacity );
        // total max: 14
        buffer.putChar( type.getTag() ); // 2 bytes
        buffer.putInt( round ); // 4 bytes
        buffer.putInt( prop_nb ); // 4 bytes
        buffer.putInt( src ); // 4 bytes
        return buffer;
    }

    public DatagramPacket getDatagram()
    {
        ByteBuffer bb = getPacketBuffer( BUFFER_CAPACITY );
        return new DatagramPacket( bb.array(), bb.capacity() );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Packet that = (Packet) o;
        return super.equals( that ) && getDestId() == that.getDestId();
    }

    @Override
    public int hashCode()
    {
        int hash = super.hashCode();
        hash = 89 * hash + Objects.hash( getDestId() );
        return hash;
    }

    @Override
    public String toString()
    {
        return super.toString() + ", DST=" + getDestId();
    }

    @Override
    protected Comparator<Message> getComparator()
    {
        return super.getComparator()
            .thenComparing( m -> ((Packet) m).getDestId() );
    }

    public int getDestId() { return dest.getId(); }
    public int getDestPort() { return dest.getPort(); }
    public InetAddress getDestAddress() { return dest.getAddress(); }

    public static Comparator<Packet> getAckComparator()
    {
        return Comparator
            .comparing( Packet::getRound )
            .thenComparing( Packet::getPropNb )
            .thenComparing( (a, b) -> Integer.compare( a.getDestId(), b.getSrc() ) )
            .thenComparing( (a, b) -> Integer.compare( a.getSrc(), b.getDestId() ) );
    }
}
