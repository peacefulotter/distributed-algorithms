package cs451.parser.perfectlink;

import cs451.Host;

import java.util.List;

public class Invoker
{
    private final Server server;
    private final Thread thread;

    public Invoker( List<Host> hosts, PLConfig config, int id, String output )
    {
        Host myHost = hosts.get( id - 1 );
        Host dest = hosts.get( config.getI() );
        this.server =  myHost.getId() == dest.getId()
            ? new Receiver( myHost, dest, output )
            : new Sender( myHost, dest, output, config );
        this.thread = new Thread( server );
    }

    public void start()
    {
        this.thread.start();
    }

    public void terminate()
    {
        this.thread.interrupt();
        this.server.closeConnection();
    }
}
