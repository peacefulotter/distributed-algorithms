package cs451.parser.packet;

import cs451.Host;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Packet
{
    private final char tag;
    private final int seqNr;
    private final int sender;

    public Packet( PacketTypes type, int seqNr, int sender )
    {
        this.tag = type.getTag();
        this.seqNr = seqNr;
        this.sender = sender;
    }

    public Packet( PacketTypes type, DatagramPacket from )
    {
        String msg = new String( from.getData() );
        this.tag = type.getTag();
        this.seqNr = Integer.parseInt( msg.trim() );
        this.sender = Host.portToId.get( from.getPort() );
        /*String[] split = msg.split( " " );
        this.tag = split[0].charAt( 0 );
        this.sender = Integer.parseInt( split[1] );
        this.seqNr = Integer.parseInt( split[2].trim() );*/
    }

    public DatagramPacket getDatagram()
    {
        String payload = seqNr + "";
        byte[] bytes = payload.getBytes( StandardCharsets.UTF_8 );
        return new DatagramPacket(bytes, bytes.length);
    }

    public String getMsg()
    {
        return tag + " " + sender + " " + seqNr;
    }
    public int getSeqNr() { return seqNr; }
    public int getSender()
    {
        return sender;
    }

    @Override
    public String toString()
    {
        return "tag: " + tag + ", seq_nr: " + seqNr + ", sender: " + sender;
    }
}
