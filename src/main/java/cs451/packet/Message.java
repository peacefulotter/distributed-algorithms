package cs451.packet;

import java.util.*;

public class Message implements Comparable<Packet>
{
    public static final int MAX = 8;

    public final PacketTypes type;
    public final int round, prop_nb, src;

    public Message( PacketTypes type, int round, int prop_nb, int src )
    {
        this.type = type;
        this.round = round;
        this.prop_nb = prop_nb;
        this.src = src;
    }

    public Message( Message m )
    {
        this( m.type, m.round, m.prop_nb, m.src );
    }

    protected Comparator<Message> getComparator()
    {
        return Comparator
            .comparing( Message::getType )
            .thenComparing( Message::getPropNb )
            .thenComparing( Message::getSrc )
            .thenComparing( Message::getRound );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Message m = (Message) o;
        return round == m.round &&
            prop_nb == m.prop_nb &&
            src == m.src &&
            type == m.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, round, prop_nb, src );
    }

    @Override
    public String toString()
    {
        return "TYPE=" + type +
            ", RND=" + round +
            ", PRP=" + prop_nb +
            ", SRC=" + src;
    }

    public PacketTypes getType() { return type; }
    public int getRound() { return round; }
    public int getPropNb() { return prop_nb; }
    public int getSrc() { return src; }

    @Override
    public int compareTo( Packet o )
    {
        return getComparator().compare( this, o );
    }
}
