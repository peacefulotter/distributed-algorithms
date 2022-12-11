package PerfectLink;

import cs451.Host;
import cs451.lat.Proposal;
import cs451.packet.PacketTypes;
import org.junit.jupiter.api.Test;

public class PacketTest
{

    @Test
    public void test() throws Exception
    {
        Host host = Host.populate("1", "127.0.0.1", "3001");
        Host dest = Host.populate("2", "127.0.0.1", "3002");

        PacketTypes t = PacketTypes.LAT_PROP;
        Proposal prop = new Proposal();
        for ( int i = 0; i < 13; i++ )
        {
            prop.add( i );
        }
//        SetPacket a = new SetPacket( t, Integer.MAX_VALUE, 1, 1, 2, prop );
//        System.out.println(a);
//        SetPacket b = (SetPacket) PacketTypes.parseDatagram( a.getDatagram(), dest );
//        System.out.println(b);
    }

}
