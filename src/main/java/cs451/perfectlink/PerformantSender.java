package cs451.perfectlink;

import cs451.Host;
import cs451.packet.Packet;
import cs451.packet.PacketTypes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class PerformantSender extends Server
{
    protected static final int MAX_RETRANSMIT_TRIES = 10;
    protected static final int MAX_NB_THREADS = 32; // TODO: proportional per sender

    private final ConcurrentLinkedQueue<Packet> delivered; // TODO: performance: garbage collection
    private final int nbMessages;

    private int seqNr, threads;
    private boolean receiverAlive;

    public PerformantSender( Host host, Host dest, String output, PLConfig config )
    {
        super( host, output );
        this.nbMessages = config.getM();
        this.seqNr = 1;
        this.threads = 1;
        this.receiverAlive = false;
        this.delivered = new ConcurrentLinkedQueue<>();

        log( "Binding to " + dest );
        try
        {
            this.socket.connect( dest.getSocketAddress() );
        } catch ( SocketException e )
        {
            terminate( e );
        }
    }

    private class PacketThread extends Thread
    {
        private final int seqNr, threadId;

        private Packet packet;
        private int retransmitTries;
        private boolean registered;

        public PacketThread( int threadId, int seqNr )
        {
            this.threadId = threadId;
            this.seqNr = seqNr;
            this.retransmitTries = 0;
            this.registered = false;
        }

        @Override
        public synchronized void start()
        {
            packet = new Packet( PacketTypes.BROADCAST, seqNr, host.getId() );
            super.start();
        }

        private Packet broadcastPacket()
        {
            try
            {
                log( threadId + "", "Sending packet " + packet );
                sendPacket( packet );
            } catch ( PortUnreachableException | ClosedChannelException e )
            {
                log( threadId + "", e.getMessage() );
                // prevent considering this as a retransmit try
                retransmitTries--;
                return null;
            } catch ( IOException e )
            {
                terminate( e );
            }

            return packet;
        }

        private boolean waitForAck()
        {
            setTimeout();
            DatagramPacket packet = getIncomingPacket();

            // didn't receive ack in the timeout or something went wrong
            if ( packet == null )
            {
                log( threadId + "", "Receiver alive: " + receiverAlive + " Failed to receive ACK for seq_nr: " + seqNr );
                if ( receiverAlive )
                    timeout.increase();
                return false;
            }

            return onAck( packet );
        }

        private boolean onAck( DatagramPacket packet )
        {
            Packet ack = new Packet( PacketTypes.ACK, packet );
            if ( ack.getSeqNr() != seqNr )
                return false;
            log(threadId + "","Received ACK: " + ack );
            deliverPacket( ack );
            return true;
        }

        private void release()
        {
            try { Thread.sleep(retransmitTries * 10L ); } // FIXME: experimental
            catch ( InterruptedException e )
            {
                throw new RuntimeException( e );
            }
        }

        private void prepareRetransmit()
        {
            if ( !receiverAlive ) return;
            log( threadId + "","Retransmit tries: " + ++retransmitTries + " / " + MAX_RETRANSMIT_TRIES );
            if ( retransmitTries >= MAX_RETRANSMIT_TRIES )
                terminate();
            else
                release();
        }

        @Override
        public void run()
        {
            boolean done = false;
            while ( !closed && !done && retransmitTries < MAX_RETRANSMIT_TRIES )
            {
                // broadcast packet to receiver
                Packet bc = broadcastPacket();
                // if broadcast failed
                if ( bc == null )
                {
                    prepareRetransmit();
                    continue;
                }
                else if ( !registered )
                {
                    registered = true;
                    handler.register( bc );
                }

                // wait for ack
                done = waitForAck();
            }

            if ( retransmitTries > MAX_RETRANSMIT_TRIES )
                terminate();
        }
    }

    private void setTimeout()
    {
        try
        {
            socket.setSoTimeout( timeout.get() );
        } catch ( SocketException e )
        {
            terminate( e );
        }
    }

    private void deliverPacket( Packet ack )
    {
        receiverAlive = true;

        // deliver msg if not already delivered
        if ( !delivered.contains( ack ) )
        {
            log( "Delivering " + ack );
            delivered.add( ack );
            threads--;
            timeout.decrease();
        }

        // TODO: garbage collection here
    }



    @Override
    protected boolean runCallback()
    {
        log(delivered.stream().map( p -> p.getSeqNr() + "" ).collect( Collectors.toList()).toString());

        if ( seqNr <= nbMessages && threads < MAX_NB_THREADS)
        {
            log( "Spawning thread: " + threads + " for seqNr: " + seqNr );
            new PacketThread( threads, seqNr ).start();
            threads++;
            seqNr++;
        } else if ( delivered.size() >= nbMessages )
        {
            log( "Done transmitting" );
            return false;
        }

        return true;
    }
}
