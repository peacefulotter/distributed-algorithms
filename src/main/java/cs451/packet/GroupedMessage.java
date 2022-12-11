package cs451.packet;

import java.util.*;

public class GroupedMessage
{
    public final int src;
    public final List<PacketContent> contents;

    public int seq;

    public GroupedMessage( int seq, int src, PacketContent content )
    {
        this(seq, src, List.of(content) );
    }

    public GroupedMessage( int seq, int src, List<PacketContent> contents )
    {
        this.seq = seq;
        this.src = src;
        this.contents = contents;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        GroupedMessage m = (GroupedMessage) o;
        return seq == m.seq &&
            src == m.src &&
            contents.equals( m.contents );
    }

    @Override
    public int hashCode()
    {
        int hash = contents.hashCode();
        hash = 89 * hash + Objects.hash( seq, src );
        return hash;
    }

    @Override
    public String toString()
    {
        String base = "SEQ=" + seq + ", SRC=" + src + ", ";
        StringJoiner j = new StringJoiner( ", " );
        for ( PacketContent c : contents )
            j.add( c.toString() );
        return base + j;
    }

    public int getSeq() { return seq; }
    public int getSrc() { return src; }
}
