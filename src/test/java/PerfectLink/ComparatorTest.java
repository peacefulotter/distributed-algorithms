package PerfectLink;

import cs451.Host;
import cs451.packet.PacketTypes;
import org.junit.jupiter.api.Test;


public class ComparatorTest
{
    @Test
    public void test() throws Exception
    {
        for ( int i = 0; i < 5; i++ )
        {
            Host.populate(String.valueOf( i ), "127.0.0.1", "300" + i);
        }


        // Comparator<Packet> c = Packet.getAckComparator();
        for ( int r = 0; r < 5; r++ )
        {
            for ( int p = 0; p < 3; p++ )
            {
                for ( int s = 0; s < 5; s++ )
                {
                    for ( int d = 0; d < 4; d++ )
                    {
//                        Packet a = new Packet( PacketTypes.BRC, r, p, s, d );
//                        Packet b = new Packet( PacketTypes.ACK, p, r, d, s );
//                        System.out.println( a + " " + b + " " + c.compare( a, b ) );
                    }
                }
            }
        }
    }
}
