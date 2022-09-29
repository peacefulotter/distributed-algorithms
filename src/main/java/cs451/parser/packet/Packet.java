package cs451.parser.packet;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

abstract public class Packet
{
    protected final char tag;
    protected final int seqNr;

    public Packet( char tag, int seqNr )
    {
        this.tag = tag;
        this.seqNr = seqNr;
    }

    public Packet( byte[] bytes, int seqNrIndex )
    {
        System.out.println("[Packet] received: " + new String(bytes) );
        ByteBuffer wrapped = ByteBuffer.wrap( bytes ); // big-endian by default
        this.tag = wrapped.getChar( 0 );
        this.seqNr = wrapped.getInt( seqNrIndex );
    }

    protected DatagramPacket toDatagramPacket( String msg )
    {
        byte[] bytes = msg.getBytes( StandardCharsets.UTF_8 );
        return new DatagramPacket(bytes, bytes.length);
    }

    abstract public DatagramPacket getDatagram();

    public char getTag()
    {
        return tag;
    }

    public int getSeqNr()
    {
        return seqNr;
    }

    @Override
    public String toString()
    {
        return "tag: " + tag + ", seq_nr: " + seqNr;
    }
}
