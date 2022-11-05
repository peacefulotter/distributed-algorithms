package PerfectLink;

import cs451.network.Pool;
import cs451.Main;
import cs451.parser.ParserResult;
import cs451.network.SocketService;
import cs451.utils.Logger;
import org.junit.jupiter.api.Test;

class PerfectLinkTest
{
    // ../template_java/build.sh
    // python3 tc.py
    // python3 stress.py -r ../template_java/run.sh -t perfect -l ./output -p 5 -m 250

    protected static void serverTest( SocketService service, Logger.Color color )
    {
        new Thread( () -> {
            Logger.addColor( color );
            Main.initSignalHandlers( service );
            long t1 = System.nanoTime();
            Pool pool = Main.invokeServer( service );
            pool.start();
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

//    protected static SocketService sender( int id, Logger.Color color )
//    {
//        ParserResult r = Main.parseArgs( getArgs( id ) );
//        SocketService s = new PLSender( r.host, r.dest, r.output, r.config );
//        s.setColor( color );
//        return s;
//    }
//
//    protected static SocketService receiver( int id )
//    {
//        ParserResult r = Main.parseArgs( getArgs( id ) );
//        SocketService s = new Receiver( r.host, r.output );
//        s.setColor( Logger.Color.RED );
//        return s;
//    }
//
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

    protected SocketService getService( int id )
    {
        ParserResult result = Main.parseArgs( getArgs( id ) );
        return new SocketService( result );
    }

    @Test
    public void perfectLinkTest()
    {
        SocketService s1 = getService( 1 );
        SocketService s2 = getService( 2 );
        SocketService s3 = getService( 3 );

        serverTest( s1, Logger.Color.GREEN );
        serverTest( s2, Logger.Color.RED );
        serverTest( s3, Logger.Color.BLUE );

        hold(60 * 60 * 1000);
    }

}