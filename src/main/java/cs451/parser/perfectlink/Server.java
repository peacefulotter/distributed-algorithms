package cs451.parser.perfectlink;

import cs451.Host;
import cs451.parser.packet.Packet;

import java.io.IOException;
import java.net.*;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

abstract public class Server
{
    private final byte[] buf = new byte[256];

    protected final DatagramSocket socket;
    protected final FileHandler handler;
    protected final Host host;

    protected final List<String> delivered;
    protected final Timeout timeout;

    private boolean running;

    public Server( Host host, String output )
    {
        this.host = host;
        this.delivered = new ArrayList<>();
        this.handler = new FileHandler( output );
        this.timeout = new Timeout();
        this.running = false;

        try
        {
            this.socket = new DatagramSocket( host.getSocketAddress() );
        } catch ( SocketException e )
        {
            throw new RuntimeException( e );
        }

        System.out.println( host + "Socket connected" );
    }

    protected DatagramPacket getIncomingPacket()
    {
        try
        {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            return packet;
        }
        catch ( PortUnreachableException | ClosedChannelException ignored ) {}
        catch ( IOException e )
        {
            terminate(e);
        }
        return null;
    }

    /**
     * Send a packet to the dest specified by func(dg) or to the connected socket by default
     */
    protected void sendPacket( Packet packet, DatagramFunc func ) throws IOException
    {
        DatagramPacket datagram = packet.getDatagram();
        func.setDest( datagram );
        socket.send( datagram );
    }

    /**
     * Send a packet to the connected socket
     */
    protected void sendPacket( Packet packet ) throws IOException
    {
        sendPacket( packet, (dg) -> {} );
    }

    abstract protected boolean runCallback();

    public void run()
    {
        running = true;
        System.out.println(host + "Socket running...");

        while (running)
            running = runCallback();

        terminate();
    }

    public void terminate( Exception e )
    {
        e.printStackTrace();
        terminate();
    }

    public void terminate()
    {
        System.out.println( host + "Closing connection" );
        running = false;
        socket.close();
        handler.write();
    }
}
