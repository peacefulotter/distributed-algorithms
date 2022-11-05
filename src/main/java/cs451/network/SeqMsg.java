package cs451.network;

public class SeqMsg
{
    public final int seqNr, messages;

    public SeqMsg( int seqNr, int messages )
    {
        this.seqNr = seqNr;
        this.messages = messages;
    }

    @Override
    public String toString()
    {
        return "(" + seqNr + ", " + messages + ")";
    }
}
