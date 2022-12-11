package cs451.network;

import cs451.Host;
import cs451.lat.Proposal;
import cs451.packet.GroupedPacket;
import cs451.packet.PacketContent;
import cs451.packet.PacketParser;
import cs451.parser.ParserResult;
import cs451.utils.FileHandler;
import cs451.utils.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketService
{
    private final byte[] buf;

    private final FileHandler handler;
    private final List<Host> hosts;

    protected final DatagramSocket socket;
    protected final Host host;

    public final Queue<PacketContent> proposals;
    public final AtomicBoolean closed;
    public final Timeout timeout;
    public final int id;

    public SocketService( ParserResult result )
    {
        this.host = result.host;
        this.hosts = result.hosts;
        this.proposals = result.config.getContentsQueue();
        // Allocate buffer with maximum packet size
        int maxBufSize = PacketParser.maxBufSize(result.config.ds);
        this.buf = new byte[maxBufSize];

        this.id = host.getId();
        try
        {
            this.socket = new DatagramSocket( host.getSocketAddress() );
        } catch ( SocketException e )
        {
            throw new RuntimeException( e );
        }

        this.handler = new FileHandler( result.output );
        this.timeout = new Timeout( hosts );
        this.closed = new AtomicBoolean(false);
        Logger.log( id,"Socket connected" );
    }

    public int getNbHosts() { return hosts.size(); }

    public Host getSelf() { return host; }

    public List<Host> getHosts() { return hosts; }

    public GroupedPacket getIncomingPacket()
    {
        try
        {
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            socket.receive( dp );
            return PacketParser.parse( dp, id );
        }
        catch ( SocketTimeoutException | PortUnreachableException | ClosedChannelException e ) {
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
    public boolean sendPacket( GroupedPacket packet, DatagramFunc func )
    {
        DatagramPacket datagram = PacketParser.format( packet );
        func.setDest( datagram );
        try
        {
            socket.send( datagram );
        }
        /*catch ( PortUnreachableException e )
        {
            return false;
        }*/
        catch ( IOException e )
        {
            terminate( e );
            return false;
        }
        return true;
    }

    public void registerProposal( Proposal proposal )
    {
        this.handler.register( proposal );
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
