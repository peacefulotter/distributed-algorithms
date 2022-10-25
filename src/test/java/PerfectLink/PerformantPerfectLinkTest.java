package PerfectLink;

import cs451.Logger;
import cs451.Main;
import cs451.parser.ParserResult;
import cs451.perfectlink.PerformantSender;
import cs451.perfectlink.Receiver;
import cs451.perfectlink.Sender;
import cs451.perfectlink.Server;
import org.junit.jupiter.api.Test;

class PerformantPerfectLinkTest extends PerfectLinkTest
{
    protected Server performantSender( int id, Logger.Color color )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new PerformantSender( r.host, r.dest, r.output, r.config );
        s.setColor( color );
        return s;
    }

    @Test
    public void droppingSenderReceiverTest()
    {
        Server s1 = performantSender( 1, Logger.Color.BLUE );
        Server s2 = receiver( 2, Logger.Color.RED );
        Server s3 = performantSender(3, Logger.Color.GREEN );

        serverTest( s1 );
        serverTest( s2 );
        serverTest( s3 );

        hold();
    }
}
