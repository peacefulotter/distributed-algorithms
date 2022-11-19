package cs451.packet;

import cs451.Host;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Objects;

public class Packet extends Message
{
    private final Host dest;

    public Packet( PacketTypes type, int seq, int origin, int src, Host dest, int messages )
    {
        super(type, seq, origin, src, messages);
        this.dest = dest;
    }

    public Packet( Message msg, Host dest )
    {
        super( msg );
        this.dest = dest;
    }

    public Packet( DatagramPacket from, Host dest )
    {
        super( from );
        this.dest = dest;
    }


    private static Packet inverse( PacketTypes type, Packet packet )
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
        return new Packet( pt, seq, origin, src, dest, messages );
    }

    public Packet getRelay()
    {
        return new Packet( type, seq, origin, origin, dest, messages );
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
        return Objects.hash( type, seq, origin, src, getDestId() );
    }

    @Override
    public String toString()
    {
        return "TYPE=" + type +
            ", ORG=" + origin +
            ", SRC=" + src +
            ", DST=" + getDestId() +
            ", SEQ=" + seq +
            ", MSG=" + messages;
    }

    @Override
    public int compareTo( Packet o )
    {
        return getComparator()
            .thenComparing( m -> ((Packet) m).getDestId() )
            .compare( this, o );
    }

    public int getDestId() { return dest.getId(); }
    public int getDestPort() { return dest.getPort(); }
    public InetAddress getDestAddress() { return dest.getAddress(); }
}
