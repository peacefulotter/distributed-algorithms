package cs451.perfectlink;

import cs451.Host;
import cs451.packet.Packet;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Sender extends Server
{
    private static final int N_THREADS = 3;
    private static final int MAX_MSGS_PER_PACKET = 8;

    private final ConcurrentLinkedQueue<Packet> delivered; // TODO: performance: garbage collection
    private final ThreadPool pool;
    private final int nbMessages;

    public Sender( Host host, Host dest, String output, PLConfig config )
    {
        super( host, output );
        this.nbMessages = config.getM();
        this.delivered = new ConcurrentLinkedQueue<>();
        this.pool = new ThreadPool( this, N_THREADS );

        log( "Binding to " + dest );
        try
        {
            this.socket.connect( dest.getSocketAddress() );
        } catch ( SocketException e )
        {
            terminate( e );
        }
    }

    protected boolean setTimeout()
    {
        try { socket.setSoTimeout( timeout.get() ); }
        catch ( SocketException e ) {
            terminate( e );
            return false;
        }
        return true;
    }

    protected void deliverPacket( Packet ack )
    {
        // deliver msg if not already delivered
        if ( !delivered.contains( ack ) )
        {
            log( "Delivering " + ack );
            delivered.add( ack );
            timeout.decrease();
        }

        // TODO: garbage collection here
    }

    @Override
    protected boolean runCallback()
    {
        // TODO: MAX SUBMIT TASKS
        int seqNr;
        for ( seqNr = 1; seqNr < nbMessages; seqNr += MAX_MSGS_PER_PACKET )
        {
            // try to send a maximum of 8 messages per packet
            int messages = Math.min(MAX_MSGS_PER_PACKET, nbMessages - seqNr + 1);
            pool.submitTask( seqNr, messages );
        }

        /*seqNr -= MAX_MSGS_PER_PACKET;
        int rest = nbMessages % MAX_MSGS_PER_PACKET;
        if ( rest > 0 )
        {
            log( "======================== MOD " + seqNr  + "  " + rest );
            pool.submitTask( seqNr, rest );
        }*/

        return pool.awaitTermination();
    }

    @Override
    public void terminate()
    {
        pool.shutdown();
        super.terminate();
    }
}
