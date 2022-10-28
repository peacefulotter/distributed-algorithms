package PerfectLink;

import cs451.Logger;
import cs451.Main;
import cs451.parser.ParserResult;
import cs451.perfectlink.Receiver;
import cs451.perfectlink.Sender;
import cs451.perfectlink.Server;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Arrays;

class PerfectLinkTest
{
    // ../template_java/build.sh
    // python3 tc.py
    // python3 stress.py -r ../template_java/run.sh -t perfect -l ./output -p 5 -m 250

    protected static void serverTest( Server server )
    {
        new Thread( () -> {
            Main.initSignalHandlers( server );
            long t1 = System.nanoTime();
            server.run();
            long t2 = System.nanoTime();
            long delta = (t2 - t1) / 1000000;
            System.out.println(delta + "ms");
        } ).start();
    }

    protected static String[] getArgs( int id )
    {
        return new String[] {
            "--id", id + "",
            "--hosts", "../example/hosts",
            "--output", "../example/output/" + id + ".output",
            "../example/configs/perfect-links.config"
        };
    }

    protected static Server sender( int id, Logger.Color color )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new Sender( r.host, r.dest, r.output, r.config );
        s.setColor( color );
        return s;
    }

    protected static Server receiver( int id )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new Receiver( r.host, r.output );
        s.setColor( Logger.Color.RED );
        return s;
    }

    public static Server droppingSender( int id, Logger.Color color )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new DroppingSender( r );
        s.setColor( color );
        return s;
    }

    protected static Server droppingReceiver( int id )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new DroppingReceiver( r );
        s.setColor( Logger.Color.RED );
        return s;
    }

    protected static void hold( long time )
    {
        try
        {
            Thread.sleep( time );
        } catch ( InterruptedException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Test
    public void perfectLinkTest()
    {
        Server s1 = sender( 1, Logger.Color.BLUE );
        Server s2 = receiver( 2 );
        Server s3 = sender(3, Logger.Color.GREEN );

        serverTest( s1 );
        serverTest( s2 );
        serverTest( s3 );

        hold(60 * 60 * 1000);
    }

    @Test
    public void droppingReceiverTest()
    {
        Server s1 = sender( 1, Logger.Color.BLUE );
        Server s2 = droppingReceiver( 2 );
        Server s3 = sender(3, Logger.Color.GREEN );

        serverTest( s1 );
        serverTest( s2 );
        serverTest( s3 );

        hold(60 * 60 * 1000);
    }

    @Test
    public void droppingSenderTest()
    {
        Server s1 = droppingSender( 1, Logger.Color.BLUE );
        Server s2 = receiver( 2 );
        Server s3 = droppingSender(3, Logger.Color.GREEN );

        serverTest( s1 );
        serverTest( s2 );
        serverTest( s3 );

        hold(60 * 60 * 1000);
    }

    @Test
    public void droppingSenderReceiverTest()
    {
        Server s1 = droppingSender( 1, Logger.Color.BLUE );
        Server s2 = droppingReceiver( 2 );
        Server s3 = droppingSender(3, Logger.Color.GREEN );

        serverTest( s1 );
        serverTest( s2 );
        serverTest( s3 );

        hold(60 * 60 * 1000);
    }
}
