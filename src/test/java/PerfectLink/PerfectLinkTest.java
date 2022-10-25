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
    protected static void serverTest( Server server )
    {
        new Thread( () -> {
            Main.initSignalHandlers( server );
            server.run();
        } ).start();
    }

    protected String[] getArgs( int id )
    {
        return new String[] {
            "--id", id + "",
            "--hosts", "../example/hosts",
            "--output", "../example/output/" + id + ".output",
            "../example/configs/perfect-links.config"
        };
    }

    protected Server sender( int id, Logger.Color color )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new Sender( r.host, r.dest, r.output, r.config );
        s.setColor( color );
        return s;
    }

    protected Server receiver( int id, Logger.Color color )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new Receiver( r.host, r.output );
        s.setColor( color );
        return s;
    }

    protected Server droppingSender( int id, Logger.Color color )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new DroppingSender( r );
        s.setColor( color );
        return s;
    }

    protected Server droppingReceiver( int id, Logger.Color color )
    {
        ParserResult r = Main.parseArgs( getArgs( id ) );
        Server s = new DroppingReceiver( r );
        s.setColor( color );
        return s;
    }

    protected void hold()
    {
        try
        {
            Thread.sleep( 60 * 60 * 1000 );
        } catch ( InterruptedException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Test
    public void droppingReceiverTest()
    {
        Server s1 = sender( 1, Logger.Color.BLUE );
        Server s2 = droppingReceiver( 2, Logger.Color.RED );
        Server s3 = sender(3, Logger.Color.GREEN );

        serverTest( s1 );
        serverTest( s2 );
        serverTest( s3 );

        hold();
    }

    @Test
    public void droppingSenderTest()
    {
        Server s1 = droppingSender( 1, Logger.Color.BLUE );
        Server s2 = receiver( 2, Logger.Color.RED );
        Server s3 = droppingSender(3, Logger.Color.GREEN );

        serverTest( s1 );
        serverTest( s2 );
        serverTest( s3 );

        hold();
    }

    @Test
    public void droppingSenderReceiverTest()
    {
        Server s1 = droppingSender( 1, Logger.Color.BLUE );
        Server s2 = droppingReceiver( 2, Logger.Color.RED );
        Server s3 = droppingSender(3, Logger.Color.GREEN );

        serverTest( s1 );
        serverTest( s2 );
        serverTest( s3 );

        hold();
    }
}
