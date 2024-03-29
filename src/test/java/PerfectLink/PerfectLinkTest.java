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
    // python3 stress.py -r ../template_java/run.sh -t fifo -l ./output -p 3 -m 25
    // python3 stress.py -r ../template_java/run.sh -t fifo -l ./output -p 5 -m 500
    // python3 validate_fifo.py --proc_num 5 --output ./output/

    // python3 ./stress.py agreement -r ../template_java/run.sh -l ./output -p 3 -n 5 -v 10 -d 5

    protected static void serverTest( SocketService service, Logger.Color color )
    {
        Logger.addColor( service.id, color );
        new Thread( () -> {
            Main.initSignalHandlers( service );
            Pool pool = Main.invokeLATServer( service );
            pool.start();
        } ).start();
    }

    protected static String[] getArgs( String mode, int id )
    {
        return new String[] {
            "--id", id + "",
            "--hosts", "../example/hosts",
            "--output", "../example/output/" + id + ".output",
            // "../example/configs/lattice-agreement-" + id + ".config"
            "../example/configs/" + mode + "/custom-" + id + ".config"
        };
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

    protected SocketService getService( String mode, int id )
    {
        ParserResult result = Main.parseArgs( getArgs( mode, id ) );
        return new SocketService( result );
    }

    @Test
    public void perfectLinkTest()
    {
        String mode = "hard";
        SocketService s1 = getService( mode, 1 );
        SocketService s2 = getService( mode, 2 );
        SocketService s3 = getService( mode, 3 );
        SocketService s4 = getService( mode, 4 );
        SocketService s5 = getService( mode, 5 );

        serverTest( s1, Logger.Color.BLUE );
        // serverTest( s2, Logger.Color.RED );
        serverTest( s3, Logger.Color.GREEN );
        // serverTest( s4, Logger.Color.BLUE );
        serverTest( s5, Logger.Color.RED );

        hold(60 * 60 * 1000);
    }
}
