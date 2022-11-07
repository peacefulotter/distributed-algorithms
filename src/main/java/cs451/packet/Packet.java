package cs451.packet;

import cs451.Host;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Packet
{
    private static final String SEPARATOR = "-";

    private final PacketTypes type;
    private final Host dest;
    private final int seqNr, src, messages;

    public Packet( PacketTypes type, int seqNr, int src, Host dest, int messages )
    {
        this.type = type;
        this.seqNr = seqNr;
        this.src = src;
        this.dest = dest;
        this.messages = messages;
    }

    private static Packet inverseDirection( PacketTypes type, Packet packet )
    {
        int src = packet.getDestId();
        Host dest = Host.findById.get( packet.getSrc() );
        return new Packet( type, packet.getSeqNr(), src, dest, packet.getMessages() );
    }

    /**
     * Inverse direction and returns an ACK packet
     */
    public static Packet createACKPacket( Packet packet )
    {
        return inverseDirection( PacketTypes.ACK, packet );
    }

    /**
     * Inverse direction and returns a BROADCAST packet
     */
    public static Packet createBRCPacket( Packet packet )
    {
        return inverseDirection( PacketTypes.BROADCAST, packet );
    }

    public Packet( DatagramPacket from )
    {
        String msg = new String( from.getData() );
        String[] split = msg.split( SEPARATOR );
        this.type = PacketTypes.parseType( msg.charAt( 0 ) );
        this.seqNr = intParse( split[1] );
        this.src = intParse( split[2] );
        this.dest = Host.findById.get( intParse( split[3] ) );
        this.messages = intParse( split[4] );
    }

    public Packet withType( PacketTypes pt )
    {
        return new Packet( pt, seqNr, src, dest, messages );
    }

    private int intParse(String m)
    {
        return Integer.parseInt( m.trim() );
    }

    private String getPayloads()
    {
        StringJoiner sj = new StringJoiner( SEPARATOR );
        for ( int i = seqNr; i < seqNr + messages; i++ )
            sj.add( i + "" );
        return sj.toString();
    }

    public DatagramPacket getDatagram()
    {
        // total max: 70
        String payload = new StringJoiner( SEPARATOR ) // messages + 5 (max 13)
            .add( type.getTag() + "" ) // 2 bytes
            .add( seqNr + "" ) // 4 bytes
            .add( src + "" ) // 4 bytes
            .add( getDestId() + "" ) // 4 bytes
            .add( messages + "" ) // 4 bytes
            .add( getPayloads() ) // messages * 4 bytes + messages - 1 (max 39)
            .toString();
        byte[] bytes = payload.getBytes( StandardCharsets.UTF_8 );
        return new DatagramPacket(bytes, bytes.length);
    }
    public List<String> getFileLines() { return type.getFileLines( this ); }

    public PacketTypes getType() { return type; }
    public int getSeqNr() { return seqNr; }
    public int getSrc() { return src; }
    public int getDestId() { return dest.getId(); }
    public int getDestPort() { return dest.getPort(); }
    public InetAddress getDestAddress() { return dest.getAddress(); }
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
        Packet that = (Packet) o;
        return
            type == that.type &&
            seqNr == that.seqNr &&
            src == that.src &&
            getDestId() == that.getDestId() &&
            messages == that.messages;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, seqNr, src, dest, messages );
    }

    @Override
    public String toString()
    {
        return "TYPE=" + type +
            ", SRC=" + src +
            ", DEST=" + getDestId() +
            ", SEQ=" + seqNr +
            ", MSGS=" + messages;
    }
}
