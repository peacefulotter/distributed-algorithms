package cs451;

import cs451.parser.HostsParser;
import cs451.parser.Parser;
import cs451.parser.perfectlink.*;

import java.util.List;
import java.util.stream.Collectors;

public class Main {


    private static void initSignalHandlers( Server server )
    {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            //immediately stop network packet processing
            System.out.println("Immediately stopping network packet processing.");
            server.terminate();

            //write/flush output file if necessary
            System.out.println("Writing output.");
        } ) );
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        HostsParser hostsParser = parser.parse();

        PLConfig config = new PLConfig( parser.config() );
        List<Host> hosts = hostsParser.getHosts();

        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\t My ID: " + parser.myId() + "\n");
        System.out.println("`kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
        for (Host host: hosts) {
            System.out.println(host.getId() + " - IP: " + host.getIp() + " - Port: " + host.getPort() );
        }
        System.out.println("\nPath to output: " + parser.output());
        System.out.println("Path to config: " + parser.config() + "\n");

        int id = parser.myId();
        String output = parser.output();
        Host myHost = hosts.get( id - 1 );
        Host dest = hosts.get( config.getI() );
        Server server =  myHost.getId() == dest.getId()
            ? new Receiver( myHost, output )
            : new Sender( myHost, dest, output, config );

        initSignalHandlers( server );

        server.run();
    }
}
