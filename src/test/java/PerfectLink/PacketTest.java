package PerfectLink;

import cs451.Host;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PacketTest
{
    @Test
    public void test() throws Exception
    {
        Host dest = Host.populate( "1", "127.0.0.1", "3000" );
        System.out.println(dest);
        Host.populate( "2", "127.0.0.1", "3001" );
        System.out.println(Host.findById);
        for ( int i = 1; i < 100; i += 8 )
        {
            Packet p = new Packet( PacketTypes.BRC, i, 3, 2, dest, 8 );
            Packet p_ = new Packet( p.getDatagram(), dest );
            Packet a = Packet.createACKPacket( p );
            Packet b = Packet.createBRCPacket( a );
            System.out.println(p.compareTo( p_ ) + " " + p_.compareTo( p ) + " " + p.compareTo( a ) + " " + p.compareTo( b ));
            assertEquals(p, p_);
        }
    }
}
