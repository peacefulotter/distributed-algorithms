package cs451.network;

import cs451.Host;
import cs451.lat.Proposal;
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
        this.timeout = new Timeout( id, hosts );
        this.closed = new AtomicBoolean(false);
        Logger.log( id,"Socket connected" );
    }

    public int getNbHosts() { return hosts.size(); }

    public List<Host> getHosts() { return hosts; }

    public DatagramPacket getIncomingPacket()
    {
        try
        {
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            socket.receive( dp );
            return dp;
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
    public boolean sendPacket( DatagramPacket dg, int destId )
    {
        try
        {
            final Host dest = Host.get( destId );
            dg.setAddress( dest.getAddress() );
            dg.setPort( dest.getPort() );
            socket.send( dg );
        }
        catch ( IOException e )
        {
            terminate( e );
            return false;
        }
        return true;
    }

    public void registerProposal( int round, Proposal proposal )
    {
        this.handler.register( round, proposal );
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
        handler.write( id );
        socket.close();
    }
}
