package PerfectLink;

import cs451.Host;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import org.junit.jupiter.api.Test;

public class PacketTest
{
    @Test
    public void test() throws Exception
    {
        Host dest = Host.populate( "1", "127.0.0.1", "3000" );
        Packet p = new Packet( PacketTypes.BRC, 17, 2, 99, dest, 8 );
        Packet p_ = new Packet( p.getDatagram(), dest );
        System.out.println(p);
        System.out.println(p_);
    }
}
