package cs451.packet;

import cs451.Host;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Packet
{
    private final PacketTypes type;
    private final int seqNr;
    private final int sender;

    public Packet( PacketTypes type, int seqNr, int sender )
    {
        this.type = type;
        this.seqNr = seqNr;
        this.sender = sender;
    }

    public Packet( PacketTypes type, DatagramPacket from )
    {
        String msg = new String( from.getData() );
        this.type = type;
        this.seqNr = Integer.parseInt( msg.split("-")[0].trim() );
        this.sender = Host.portToId.get( from.getPort() );
    }

    public DatagramPacket getDatagram()
    {
        String payload = seqNr + "-";
        byte[] bytes = payload.getBytes( StandardCharsets.UTF_8 );
        return new DatagramPacket(bytes, bytes.length);
    }

    public String getMsg()
    {
        return type + " " + sender + " " + seqNr;
    }
    public String getFileLine() { return type.getFileLine( this ); }
    public int getSeqNr() { return seqNr; }
    public int getSender()
    {
        return sender;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        Packet packet = (Packet) o;
        return seqNr == packet.seqNr && sender == packet.sender && type == packet.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, seqNr, sender );
    }

    @Override
    public String toString()
    {
        return "type: " + type + ", seq_nr: " + seqNr + ", sender: " + sender;
    }
}
