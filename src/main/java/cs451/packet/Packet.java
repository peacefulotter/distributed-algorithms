package cs451.packet;

import cs451.Host;
import cs451.utils.Logger;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    private final int seqNr, origin, src, messages;

    public Packet( PacketTypes type, int seqNr, int origin, int src, Host dest, int messages )
    {
        this.type = type;
        this.seqNr = seqNr;
        this.origin = origin;
        this.src = src;
        this.dest = dest;
        this.messages = messages;
    }

    public Packet( Message msg, Host dest )
    {
        this( msg.type, msg.seq, msg.origin, msg.src, dest, msg.messages );
    }

    private static Packet inverseDirection( PacketTypes type, Packet packet )
    {
        int src = packet.getDestId();
        Host dest = Host.findById.get( packet.getSrc() );
        return new Packet( type, packet.getSeqNr(), packet.getOrigin(), src, dest, packet.getMessages() );
    }

    /**
     * Inverse direction and returns an ACK packet
     */
    public static Packet createACKPacket( Packet packet )
    {
        return inverseDirection( PacketTypes.ACK, packet );
    }

    /**
     * Inverse direction and returns a BRC packet
     */
    public static Packet createBRCPacket( Packet packet )
    {
        return inverseDirection( PacketTypes.BRC, packet );
    }

    public Packet( DatagramPacket from )
    {
        String msg = new String( from.getData() ).trim();
        String[] split = msg.split( SEPARATOR );
        this.type = PacketTypes.parseType( msg.charAt( 0 ) );
        this.seqNr = Integer.parseInt( split[1] );
        this.origin = Integer.parseInt( split[2] );
        this.src = Integer.parseInt( split[3] );
        this.dest = Host.findById.get( Integer.parseInt( split[4] ) );
        this.messages = Integer.parseInt( split[5] );
    }

    public Packet withType( PacketTypes pt )
    {
        return new Packet( pt, seqNr, origin, src, dest, messages );
    }

    public Packet getRelay()
    {
        return new Packet( type, seqNr, origin, origin, dest, messages );
    }

    /**
     * converts the seqNr into the packet index
     */
    public int getIndex()
    {
        return seqNr / Message.MAX;
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
        // total max: 74
        String payload = new StringJoiner( SEPARATOR ) // messages + 5 (max 13)
            .add( type.getTag() + "" ) // 2 bytes
            .add( seqNr + "" ) // 4 bytes
            .add( origin + "" ) // 4 bytes
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
    public int getOrigin() { return origin; }
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
        return Objects.hash( type, seqNr, origin, src, dest, messages );
    }

    @Override
    public String toString()
    {
        return "TYPE=" + type +
            ", ORIG=" + origin +
            ", SRC=" + src +
            ", DEST=" + getDestId() +
            ", SEQ=" + seqNr +
            ", MSGS=" + messages;
    }
}
