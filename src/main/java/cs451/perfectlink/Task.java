package cs451.perfectlink;

public class Task
{
    public final int seqNr, messages;

    public Task( int seqNr, int messages )
    {
        this.seqNr = seqNr;
        this.messages = messages;
    }

    @Override
    public String toString()
    {
        return "seqNr=" + seqNr + ", messages=" + messages;
    }
}
