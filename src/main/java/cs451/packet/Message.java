package cs451.packet;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.*;

public class Message implements Comparable<Packet>
{
    public static final int MAX = 8;

    public final PacketTypes type;
    public final int seq, origin, src;

    public Message( PacketTypes type, int seq, int origin, int src )
    {
        this.type = type;
        this.seq = seq;
        this.origin = origin;
        this.src = src;
    }

    public Message( Message m )
    {
        this( m.type, m.seq, m.origin, m.src );
    }

    protected Comparator<Message> getComparator()
    {
        return Comparator
            .comparing( Message::getType )
            .thenComparing( Message::getOrigin )
            .thenComparing( Message::getSrc )
            .thenComparing( Message::getSeqNr );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Message m = (Message) o;
        return seq == m.seq &&
            origin == m.origin &&
            src == m.src &&
            type == m.type;
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
            ", SRC=" + src;
    }

    public PacketTypes getType() { return type; }
    public int getSeqNr() { return seq; }
    public int getOrigin() { return origin; }
    public int getSrc() { return src; }

    @Override
    public int compareTo( Packet o )
    {
        return getComparator().compare( this, o );
    }
}
