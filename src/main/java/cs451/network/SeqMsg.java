package cs451.network;

import cs451.packet.Packet;

import java.util.Objects;

public class SeqMsg
{
    public static final int MAX_MSG_PER_PACKET = 8;

    public final int seqNr, messages;

    public SeqMsg( int seqNr, int messages )
    {
        this.seqNr = seqNr;
        this.messages = messages;
    }

    public SeqMsg( Packet packet )
    {
        this( packet.getSeqNr(), packet.getMessages() );
    }

    public static SeqMsg getFirst( int nbMessages )
    {
        int messages = Math.min( nbMessages, MAX_MSG_PER_PACKET );
        return new SeqMsg( 1, messages );
    }

    public SeqMsg getNext( int nbMessages )
    {
        int nextSeq = seqNr + MAX_MSG_PER_PACKET;
        int nextMsg = Math.min( nbMessages - nextSeq + 1, MAX_MSG_PER_PACKET );
        return new SeqMsg( nextSeq, nextMsg );
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
