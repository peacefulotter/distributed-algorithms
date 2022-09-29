package cs451;

import cs451.parser.Parser;
import cs451.parser.perfectlink.PLHost;

import java.util.List;

public class Main {


    private static void initSignalHandlers( List<PLHost> hosts )
    {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            //immediately stop network packet processing
            System.out.println("Immediately stopping network packet processing.");
            hosts.forEach( PLHost::closeConnection );

            //write/flush output file if necessary
            System.out.println("Writing output.");
        } ) );
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        List<PLHost> hosts = parser.parse();

        initSignalHandlers( hosts );

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        for (Host host: hosts) {
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        System.out.println("Broadcasting and delivering messages...\n");

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
