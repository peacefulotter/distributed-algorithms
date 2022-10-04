package cs451;

import cs451.parser.HostsParser;
import cs451.parser.Parser;
import cs451.parser.perfectlink.Invoker;
import cs451.parser.perfectlink.PLConfig;

import java.util.List;

public class Main {


    private static void initSignalHandlers( Invoker invoker )
    {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            //immediately stop network packet processing
            System.out.println("Immediately stopping network packet processing.");
            invoker.terminate();

            //write/flush output file if necessary
            System.out.println("Writing output.");
        } ) );
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        HostsParser hostsParser = parser.parse();

        PLConfig config = new PLConfig( parser.config() );
        List<Host> hosts = hostsParser.getHosts();
        int myId = parser.myId();

        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\t My ID: " + parser.myId() + "\n");
        System.out.println("`kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");
        for (Host host: hosts) {
            System.out.println(host.getId() + " - IP: " + host.getIp() + " - Port: " + host.getPort() );
        }
        System.out.println("\nPath to output: " + parser.output());
        System.out.println("Path to config: " + parser.config() + "\n");

        Invoker invoker = new Invoker( hosts, config, myId, parser.output() );
        initSignalHandlers( invoker );

        invoker.start();

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
