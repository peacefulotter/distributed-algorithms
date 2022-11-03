package cs451.packet;

import cs451.Host;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BEPacket
{
    private final PacketTypes type;
    private final int seqNr, sender, dest, messages;

    public BEPacket( PacketTypes type, int seqNr, int sender, int dest, int messages )
    {
        this.type = type;
        this.seqNr = seqNr;
        this.sender = sender;
        this.dest = dest;
        this.messages = messages;
    }

    public BEPacket( PacketTypes type, DatagramPacket from )
    {
        String[] msg = new String( from.getData() ).trim().split( "-" );
        this.type = type;
        this.sender = Host.portToId.get( from.getPort() );
        this.seqNr = Integer.parseInt( msg[0].trim() );
        this.dest = Integer.parseInt( msg[1].trim() );
        this.messages = Integer.parseInt( msg[2].trim() );
    }

    public DatagramPacket getDatagram()
    {
        String payload = seqNr + "-" + sender + "-" + dest + "-" + messages + "-";
        byte[] bytes = payload.getBytes( StandardCharsets.UTF_8 );
        return new DatagramPacket(bytes, bytes.length);
    }
    public List<String> getFileLines() { return type.getFileLines( this ); }
    public int getSeqNr() { return seqNr; }
    public int getSender() { return sender; }
    public int getDest() { return dest; }
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
        BEPacket that = (BEPacket) o;
        return seqNr == that.seqNr &&
            sender == that.sender &&
            dest == that.dest &&
            messages == that.messages &&
            type == that.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, seqNr, sender, dest, messages );
    }

    @Override
    public String toString()
    {
        return "sender: " + sender + ", dest: " + dest + ", seq_nr: " + seqNr + " messages: " + messages;
    }
}
