package cs451.network;

import cs451.Host;
import cs451.utils.FileHandler;
import cs451.utils.Logger;
import cs451.utils.Sleeper;
import cs451.packet.Packet;

import java.io.IOException;
import java.net.*;
import java.nio.channels.ClosedChannelException;

public class Server
{
    private final byte[] buf = new byte[32];

    private final FileHandler handler;
    protected final DatagramSocket socket;
    protected final Host host;
    public final Timeout timeout;

    private boolean running;
    public boolean closed;

    public Server( Host host, String output )
    {
        this.host = host;
        this.handler = new FileHandler( output );
        this.running = false;

        try
        {
            System.out.println(host);
            this.socket = new DatagramSocket( host.getSocketAddress() );
            closed = false;
        } catch ( SocketException e )
        {
            throw new RuntimeException( e );
        }

        this.timeout = new Timeout( this  );
        Logger.log( "Socket connected" );
    }

    public DatagramPacket getIncomingPacket()
    {
        try
        {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            return packet;
        }
        catch ( SocketTimeoutException e ) {
            Logger.log( "TimeoutException: " + timeout.get() + "ms");
        }
        catch ( PortUnreachableException | ClosedChannelException e ) {
            Logger.log( e.getMessage() );
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
    public boolean sendPacket( Packet packet, DatagramFunc func )
    {
        DatagramPacket datagram = packet.getDatagram();
        func.setDest( datagram );
        try
        {
            socket.send( datagram );
        } catch ( IOException e )
        {
            terminate( e );
            return false;
        }
        return true;
    }

    /**
     * Send a packet to the connected socket
     */
    public boolean sendPacket( Packet packet )
    {
        return sendPacket( packet, (dg) -> {} );
    }

    public void run()
    {
        running = true;
        Logger.log( "Socket running...");

        while (running && !closed)
        {
            running = runCallback();
            Sleeper.release();
        }

        terminate();
    }

    public void register( Packet packet )
    {
        handler.register( packet );
    }

    public void terminate( Exception e )
    {
        Logger.log( e.getMessage() );
        terminate();
    }

    public void terminate()
    {
        if ( closed ) return;
        Logger.log(  "Closing connection" );
        running = false;
        closed = true;
        socket.close();
        handler.write();
    }
}
