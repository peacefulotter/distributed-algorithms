package PerfectLink;

import cs451.Host;
import cs451.lat.Proposal;
import cs451.packet.GroupedPacket;
import cs451.packet.PacketContent;
import cs451.packet.PacketParser;
import cs451.packet.PacketTypes;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

public class PacketTest
{
    private int nextInt( int max )
    {
        return (int) Math.round( Math.random() * max );
    }

    @Test
    public void test() throws Exception
    {
        Host host = Host.populate("1", "127.0.0.1", "3001");
        Host dest = Host.populate("2", "127.0.0.1", "3002");

        PacketTypes t = PacketTypes.LAT_PROP;
        List<PacketContent> contents = new ArrayList<>();
        int boundI = nextInt( 10 ) + 10;
        System.out.println("boundI: " + boundI);
        for ( int i = 0; i < boundI; i++ )
        {
            Proposal prop = new Proposal();
            int boundJ = nextInt( 30 );
            for ( int j = 0; j < boundJ; j++ )
            {
                prop.add( j );
            }
            contents.add( new PacketContent( t, nextInt( 10 ), nextInt(2), prop ) );
        }
        System.out.println(contents);

        GroupedPacket a = new GroupedPacket( 0, 1, contents, 2);
        System.out.println(a);
        DatagramPacket dp = PacketParser.format( a );
        GroupedPacket b = PacketParser.parse( dp, 2 );
        System.out.println(b);
        //        SetPacket a = new SetPacket( t, Integer.MAX_VALUE, 1, 1, 2, prop );
//        System.out.println(a);
//        SetPacket b = (SetPacket) PacketTypes.parseDatagram( a.getDatagram(), dest );
//        System.out.println(b);
    }

}
