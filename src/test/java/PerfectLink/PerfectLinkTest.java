package PerfectLink;

import cs451.Main;
import cs451.parser.ParserResult;
import cs451.perfectlink.Sender;
import cs451.perfectlink.Server;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class PerfectLinkTest
{
    private static void spawnServer( String... args )
    {
        System.out.println( "TEST" );
        System.out.println( Arrays.asList( args ) );
        new Thread( () -> {
            ParserResult result = Main.parseArgs( args );
            Server server = result.host.getId() == result.dest.getId()
                ? new DroppingReceiver( result.host, result.output )
                : new Sender( result.host, result.dest, result.output, result.config );
            Main.initSignalHandlers( server );
            server.run();
        } ).start();
    }

    public static void main( String[] args )
    {
        spawnServer( args );
    }

    @Test
    public void start() throws InterruptedException
    {
        // spawnServer(true, "--id", "1", "--hosts", "../example/hosts", "--output", "../example/output/1.output", "../example/configs/perfect-links.config");
        // spawnServer(false, "--id", "2", "--hosts", "../example/hosts", "--output", "../example/output/2.output", "../example/configs/perfect-links.config");
        // spawnServer(false, "--id", "3", "--hosts", "../example/hosts", "--output", "../example/output/3.output", "../example/configs/perfect-links.config");
        // System.out.println("here ==========================================");
        // while ( true ) Thread.sleep(60 * 60 * 1000 );
    }
}
