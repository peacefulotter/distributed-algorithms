package cs451;

import cs451.beb.BEBReceiver;
import cs451.beb.BEBSender;
import cs451.lat.LATReceiver;
import cs451.lat.LATSender;
import cs451.lat.LATService;
import cs451.parser.LATConfig;
import cs451.network.*;
import cs451.parser.HostsParser;
import cs451.parser.Parser;
import cs451.parser.ParserResult;
import cs451.pl.PLReceiver;
import cs451.pl.PLSender;
import cs451.urb.URBReceiver;
import cs451.urb.URBSender;

import java.util.List;

public class Main {

    public static void initSignalHandlers( SocketService service )
    {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            //immediately stop network packet processing
            System.out.println("Immediately stopping network packet processing.");
            service.terminate();
        } ) );
    }

    public static void printDetails( Parser parser )
    {
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\t My ID: " + parser.myId() + "\n");
        System.out.println("`kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
        System.out.println("\nPath to output: " + parser.output());
        System.out.println("Path to config: " + parser.config() + "\n");
    }

    public static ParserResult parseArgs(String... args)
    {
        Parser parser = new Parser(args);
        HostsParser hostsParser = parser.parse();
        List<Host> hosts = hostsParser.getHosts();
        LATConfig config = new LATConfig( parser.config() );

        printDetails( parser );

        int id = parser.myId();
        String output = parser.output();
        Host host = hosts.get( id - 1 );
        return new ParserResult( host, hosts, output, config );
    }

    public static Pool invokeBEBServer( SocketService service )
    {
        PLSender sender = new BEBSender( service );
        PLReceiver receiver = new BEBReceiver( service );
        return Pool.getPool( sender, receiver );
    }

    public static Pool invokeURBServer( SocketService service )
    {
        PLSender sender = new URBSender( service );
        PLReceiver receiver = new URBReceiver( service );
        return Pool.getPool( sender, receiver );
    }

    public static Pool invokeLATServer( SocketService service )
    {
        LATService lat = new LATService( service.getNbHosts() );
        PLSender sender = new LATSender( service, lat );
        PLReceiver receiver = new LATReceiver( service, lat );
        return Pool.getPool( sender, receiver );
    }

    public static void main(String[] args)
    {
        ParserResult result = parseArgs( args );
        SocketService service = new SocketService( result );
        Pool pool = invokeLATServer( service );
        initSignalHandlers( service );
        pool.start();
    }
}
