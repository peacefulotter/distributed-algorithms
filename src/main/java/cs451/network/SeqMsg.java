package cs451.network;

import java.util.Objects;

public class SeqMsg
{
    public final int seqNr, messages;

    public SeqMsg( int seqNr, int messages )
    {
        this.seqNr = seqNr;
        this.messages = messages;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        SeqMsg seqMsg = (SeqMsg) o;
        return seqNr == seqMsg.seqNr && messages == seqMsg.messages;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( seqNr, messages );
    }

    @Override
    public String toString()
    {
        return "(" + seqNr + ", " + messages + ")";
    }
}
