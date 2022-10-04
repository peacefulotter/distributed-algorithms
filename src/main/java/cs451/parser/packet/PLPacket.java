package cs451.parser.packet;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PLPacket
{
    private final char tag;
    private final int seqNr;
    private final int sender;

    public PLPacket( char tag, int seqNr, int sender )
    {
        this.tag = tag;
        this.seqNr = seqNr;
        this.sender = sender;
    }

    public PLPacket( byte[] bytes )
    {
        String[] split = new String(bytes).split( " " );
        this.tag = split[0].charAt( 0 );
        this.sender = Integer.parseInt( split[1] );
        this.seqNr = Integer.parseInt( split[2] );
        System.out.println("[Packet] " + this);
    }

    public DatagramPacket getDatagram()
    {
        byte[] bytes = getMsg().getBytes( StandardCharsets.UTF_8 );
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
