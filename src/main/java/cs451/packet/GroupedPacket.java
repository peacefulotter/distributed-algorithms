package cs451.packet;

import java.util.List;
import java.util.Objects;

public class GroupedPacket extends GroupedMessage
{
    public final int dest;

    public GroupedPacket( int seq, int src, List<PacketContent> contents, int dest )
    {
        super( seq, src, contents );
        this.dest = dest;
    }

    public MiniPacket minify()
    {
        return new MiniPacket( seq, src, dest );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        GroupedPacket that = (GroupedPacket) o;
        return super.equals( that ) && dest == that.dest;
    }

    @Override
    public int hashCode()
    {
        int hash = super.hashCode();
        hash = 89 * hash + Objects.hash( dest );
        return hash;
    }

    @Override
    public String toString()
    {
        return "DST=" + dest + ", " + super.toString();
    }
}
