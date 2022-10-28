package cs451;

import cs451.parser.HostsParser;
import cs451.parser.Parser;
import cs451.parser.ParserResult;
import cs451.perfectlink.*;

import java.util.List;

public class Main {

    public static void initSignalHandlers( Server server )
    {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            //immediately stop network packet processing
            System.out.println("Immediately stopping network packet processing.");
            server.terminate();
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
        PLConfig config = new PLConfig( parser.config() );

        printDetails( parser );

        int id = parser.myId();
        String output = parser.output();
        Host host = hosts.get( id - 1 );
        Host dest = hosts.get( config.getI() - 1 );

        ParserResult res = new ParserResult( host, dest, output, config );
        System.out.println(res);

        return res;
    }

    public static Server invokeServer( ParserResult result )
    {
        return result.host.getId() == result.dest.getId()
            ? new Receiver( result.host, result.output )
            : new Sender( result.host, result.dest, result.output, result.config );
    }

    public static void main(String[] args)
    {
        ParserResult result = parseArgs( args );
        Server server = invokeServer( result );
        initSignalHandlers( server );
        server.run();
    }
}
