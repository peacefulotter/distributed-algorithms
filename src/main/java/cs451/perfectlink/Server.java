package cs451.perfectlink;

import cs451.Host;
import cs451.Logger;
import cs451.packet.Packet;

import java.io.IOException;
import java.net.*;
import java.nio.channels.ClosedChannelException;

abstract public class Server
{
    private final byte[] buf = new byte[32];

    protected final DatagramSocket socket;
    protected final FileHandler handler;
    protected final Host host;
    protected final Timeout timeout;

    protected boolean closed;

    private Logger.Color color;
    private boolean running;

    public Server( Host host, String output )
    {
        this.host = host;
        this.handler = new FileHandler( output );
        this.timeout = new Timeout();
        this.running = false;

        try
        {
            this.socket = new DatagramSocket( host.getSocketAddress() );
            closed = false;
        } catch ( SocketException e )
        {
            throw new RuntimeException( e );
        }

        log( "Socket connected" );
    }

    protected void log( String s )
    {
        Logger.log( color, host, s );
    }

    protected void log( String prefix, String s )
    {
        log( "[" + prefix + "] " + s );
    }

    public void setColor( Logger.Color color )
    {
        this.color = color;
    }

    protected DatagramPacket getIncomingPacket()
    {
        long a = System.nanoTime();
        try
        {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            return packet;
        }
        catch ( SocketTimeoutException e )
        {
            long b = System.nanoTime();
            long delta = (b - a) / 1000000;
            log( "INCOMING PACKET TIMEOUT: " + timeout.get() + "ms, delta: " + delta + "ms");
        }
        catch ( PortUnreachableException | ClosedChannelException e ) {
            log( e.getMessage() );
        }
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
        log( "Socket running...");

        while (running && !closed)
            running = runCallback();

        terminate();
    }

    public void terminate( Exception e )
    {
        log( e.getMessage() );
        terminate();
    }

    public void terminate()
    {
        log(  "Closing connection" );
        running = false;
        closed = true;
        socket.close();
        handler.write();
    }
}
