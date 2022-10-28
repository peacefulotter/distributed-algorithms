package cs451.packet;

import cs451.Host;

import java.net.DatagramPacket;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Packet
{
    private final PacketTypes type;
    private final int seqNr, sender, messages;

    public Packet( PacketTypes type, int seqNr, int sender, int messages )
    {
        this.type = type;
        this.seqNr = seqNr;
        this.sender = sender;
        this.messages = messages;
    }

    public Packet( PacketTypes type, DatagramPacket from )
    {
        String[] msg = new String( from.getData() ).trim().split( "-" );
        this.type = type;
        this.sender = Host.portToId.get( from.getPort() );
        this.seqNr = Integer.parseInt( msg[0].trim() );
        this.messages = Integer.parseInt( msg[1].trim() );
    }

    public DatagramPacket getDatagram()
    {
        String payload = seqNr + "-" + messages + "-";
        byte[] bytes = payload.getBytes( StandardCharsets.UTF_8 );
        return new DatagramPacket(bytes, bytes.length);
    }
    public List<String> getFileLines() { return type.getFileLines( this ); }
    public int getSeqNr() { return seqNr; }
    public int getSender() { return sender; }
    public int getMessages() { return messages; }

    public Stream<Integer> getSeqRange()
    {
        return IntStream.range( seqNr, seqNr + messages )
            .boxed();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Packet packet = (Packet) o;
        return seqNr == packet.seqNr && sender == packet.sender && messages == packet.messages && type == packet.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, seqNr, sender, messages );
    }

    @Override
    public String toString()
    {
        return "sender: " + sender + ", seq_nr: " + seqNr + " messages: " + messages;
    }
}
