package cs451.network;

import cs451.Host;
import cs451.packet.PacketTypes;
import cs451.parser.ParserResult;
import cs451.utils.FileHandler;
import cs451.utils.Logger;
import cs451.packet.Packet;

import java.io.IOException;
import java.net.*;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketService
{
    private final byte[] buf = new byte[70];

    private final FileHandler handler;
    private final List<Host> hosts;

    protected final DatagramSocket socket;
    protected final Host host;

    public final AtomicBoolean closed;
    public final Timeout timeout;
    public final int id, nbMessages;

    public SocketService( ParserResult result )
    {
        this.host = result.host;
        this.hosts = result.hosts;
        this.nbMessages = result.config.getM();

        this.id = host.getId();
        try
        {
            this.socket = new DatagramSocket( host.getSocketAddress() );
        } catch ( SocketException e )
        {
            throw new RuntimeException( e );
        }

        this.handler = new FileHandler( result.output );
        this.timeout = new Timeout();
        this.closed = new AtomicBoolean(false);
        Logger.log( "Socket connected" );
    }

    public int getNbHosts() { return hosts.size(); }

    public List<Host> getHosts() { return hosts; }

    public Packet getIncomingPacket()
    {
        try
        {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive( packet );
            return new Packet( packet );
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

    public void register( Packet packet )
    {
        this.handler.register( packet );
    }

    public void registerAck( Packet packet )
    {
        register( packet.withType( PacketTypes.ACK ) );
    }

    public void terminate( Exception e )
    {
        Logger.log( e.getMessage() );
        terminate();
    }

    public void terminate()
    {
        if ( closed.get() ) return;
        Logger.log(  "SocketService", "Closing connection" );
        closed.set( true );
        handler.write();
        socket.close();
    }
}
