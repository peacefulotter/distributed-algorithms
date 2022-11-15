package PerfectLink;

import cs451.Host;
import cs451.Main;
import cs451.fifo.FIFOReceiver;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.parser.ParserResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class FifoTest
{
    protected static String[] getArgs( int id )
    {
        return new String[] {
            "--id", id + "",
            "--hosts", "../example/hosts",
            "--output", "../example/output/" + id + ".output",
            "../example/configs/fifo-broadcast.config"
        };
    }

    protected SocketService getService( int id )
    {
        ParserResult result = Main.parseArgs( getArgs( id ) );
        return new SocketService( result );
    }

    @Test
    public void test()
    {
        SocketService s = getService( 1 );
        FIFOReceiver r = new FIFOReceiver( s );
        Packet[] ps = new Packet[] {
            new Packet( PacketTypes.BRC, 1, 2, 2, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 1, 2, 3, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 9, 2, 2, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 9, 2, 3, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 33, 2, 2, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 33, 2, 3, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 25, 2, 2, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 25, 2, 3, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 17, 2, 2, Host.findById.get( 1 ), 8 ),
            new Packet( PacketTypes.BRC, 17, 2, 3, Host.findById.get( 1 ), 8 ),
        };
        Arrays.asList(ps).forEach( p -> {
            System.out.println("==== " + p);
            r.onReceiveBroadcast( p );
        });
    }
}
