package cs451.parser.packet;

import java.net.DatagramPacket;

public class BroadcastPacket extends Packet
{
    public BroadcastPacket( int seqNr )
    {
        super( 'b', seqNr );
    }

    public BroadcastPacket( byte[] bytes )
    {
        super( bytes, 4 );
    }

    @Override
    public DatagramPacket getDatagram()
    {
        return toDatagramPacket(tag + " " + this.seqNr );
    }
}
