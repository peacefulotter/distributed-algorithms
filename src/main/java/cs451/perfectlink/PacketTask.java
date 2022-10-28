package cs451.perfectlink;

import cs451.packet.Packet;
import cs451.packet.PacketTypes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketTask implements Runnable
{
    private final ThreadPool pool;
    private final Sender sender;
    private final Packet packet;
    private final int seqNr;
    private final String prefix;
    private final AtomicBoolean acknowledged;

    public PacketTask( ThreadPool pool, Sender sender, int seqNr, int messages )
    {
        this.pool = pool;
        this.sender = sender;
        this.seqNr = seqNr;
        this.packet = new Packet( PacketTypes.BROADCAST, seqNr, sender.host.getId(), messages );
        this.prefix = "TASK " + seqNr;
        this.acknowledged = new AtomicBoolean(false);
    }

    protected boolean broadcast()
    {
        try
        {
            sender.sendPacket( packet );
            sender.log( prefix, "Sent packet " + packet );
        }
        catch ( IOException e )
        {
            sender.terminate( e );
            return false;
        }

        return true;
    }

    private boolean getAcknowledgment()
    {
        // set timeout
        if ( !sender.setTimeout() )
            return false;

        DatagramPacket packet = sender.getIncomingPacket();

        sender.log(prefix, "Received" + (packet == null ? "null" : new Packet( PacketTypes.ACK, packet ).getSeqNr()) );

        // didn't receive ack in the timeout or something went wrong
        if ( packet == null )
        {
            sender.log( "TASK", "Failed to receive ACK for seq_nr: " + seqNr );
            sender.timeout.increase();
            return true;
        }

        Packet ack = new Packet( PacketTypes.ACK, packet );
        // the pool will redistribute the ack to the correct PacketTask
        if ( ack.getSeqNr() != seqNr )
        {
            pool.onAckReceive( ack );
            return true;
        }

        onAck( ack );
        return false;
    }

    protected void onAck( Packet ack )
    {
        if ( ack.getSeqNr() != seqNr ) return;
        sender.log(prefix, "Received ACK: " + ack );
        sender.deliverPacket( ack );
        acknowledged.set( true );
    }

    protected void register()
    {
        sender.handler.register( packet );
    }

    @Override
    public void run()
    {
        while ( !sender.closed && !acknowledged.get() )
        {
            if ( getAcknowledgment() )
                broadcast();
            Sleeper.release();
        }
        sender.log(prefix, " Done");
        pool.removeTask(seqNr);
    }
}
