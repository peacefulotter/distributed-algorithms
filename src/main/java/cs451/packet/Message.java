package cs451.packet;

import cs451.network.SocketService;

import java.util.Objects;

public class Message
{
    public static final int MAX = 8;

    public final PacketTypes type;
    public final int seq, origin, src, messages;

    public Message( PacketTypes type, int seq, int origin, int src, int messages )
    {
        this.type = type;
        this.seq = seq;
        this.origin = origin;
        this.src = src;
        this.messages = messages;
    }

    public Message( Packet p )
    {
        this( p.getType(), p.getSeqNr(), p.getOrigin(), p.getSrc(), p.getMessages() );
    }

    public static Message getFirst( SocketService service )
    {
        int messages = Math.min( service.nbMessages, MAX );
        return new Message( PacketTypes.BRC, 1, service.id, service.id, messages );
    }

    public Message getNext( SocketService service )
    {
        int nextSeq = seq + MAX;
        int nextMsg = Math.min( service.nbMessages - nextSeq + 1, MAX );
        return new Message( PacketTypes.BRC, nextSeq, service.id, service.id, nextMsg );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Message message = (Message) o;
        return seq == message.seq && origin == message.origin && src == message.src && messages == message.messages && type == message.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, seq, origin, src, messages );
    }

    @Override
    public String toString()
    {
        return "\nTYPE=" + type +
            ", SEQ=" + seq +
            ", ORG=" + origin +
            ", SRC=" + src +
            ", MSG=" + messages;
    }
}
