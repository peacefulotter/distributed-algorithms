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
import java.util.List;

public class Sender extends Server
{
    private static final int MAX_RETRANSMIT_TRIES = 5;

    private final int nbMessages;
    private final List<Integer> broadcasted; // TODO: performance: garbage collection

    private int seqNr, retransmitTries;
    private boolean receiverAlive;

    public Sender( Host host, Host dest, String output, PLConfig config )
    {
        super( host, output );
        this.nbMessages = config.getM();
        this.seqNr = 1;
        this.retransmitTries = 0;
        this.receiverAlive = false;
        this.broadcasted = new ArrayList<>();

        System.out.println("Binding " + host + "=> " + dest);
        try
        {
            this.socket.connect( dest.getSocketAddress() );
        } catch ( SocketException e )
        {
            terminate( e );
        }
    }

    private Packet broadcastPacket()
    {
        Packet packet = new Packet( PacketTypes.BROADCAST, seqNr, host.getId() );
        System.out.println(host + "Sending packet " + packet);

        try
        {
            sendPacket( packet );
        }
        catch ( PortUnreachableException | ClosedChannelException e ) {
            System.out.println(host + " " + e.getMessage());
            // prevent considering this as a retransmit try
            retransmitTries--;
            return null;
        }
        catch ( IOException e ) { terminate( e ); }

        return packet;
    }

    private void prepareRetransmit()
    {
        if ( !receiverAlive ) return;
        System.out.println(host + "Retransmit tries: " + ++retransmitTries + " / " + MAX_RETRANSMIT_TRIES);
        if ( retransmitTries >= MAX_RETRANSMIT_TRIES )
            terminate();
    }

    private boolean waitForAck()
    {
        // set timeout
        try { socket.setSoTimeout( timeout.get() ); }
        catch ( SocketException e ) { terminate( e ); }

        DatagramPacket packet = getIncomingPacket();

        // didn't receive ack in the timeout or something went wrong
        if ( packet == null )
        {
            System.out.println(host + "Receiver alive: " + receiverAlive + " Failed to receive ACK for seq_nr: " + seqNr);
            if ( receiverAlive )
                timeout.increase();
            return false;
        }

        onAck( packet );
        return true;
    }

    private void onAck( DatagramPacket packet )
    {
        receiverAlive = true;

        Packet ack = new Packet( PacketTypes.ACK, packet );
        System.out.println( host + "Received ACK: " + ack );
        String ackMsg = ack.getMsg();

        // deliver msg if not already delivered
        if ( !delivered.contains( ackMsg ) && ack.getSeqNr() == seqNr )
        {
            delivered.add( ackMsg );
            // go to next message
            seqNr++;
            // TODO broadcasted.remove(bc.seqNr()) ??
        }

        retransmitTries = 0;
        timeout.decrease(); // TODO: decrease here??
    }

    private boolean broadcastAndAck()
    {
        // broadcast packet to receiver
        Packet bc = broadcastPacket();
        // if broadcast failed
        if ( bc == null )
            return false;

        if ( !broadcasted.contains( bc.getSeqNr() ) )
        {
            broadcasted.add( bc.getSeqNr() );
            // write broadcast msg
            handler.register( bc );
        }

        // wait for ack
        return waitForAck();
    }

    @Override
    protected boolean runCallback()
    {
        boolean _running = true;

        if ( seqNr <= nbMessages )
        {
            boolean broadcasted = broadcastAndAck();
            if ( !broadcasted )
            {
                prepareRetransmit();
                _running = retransmitTries <= MAX_RETRANSMIT_TRIES;
            }
        }
        else
        {
            System.out.println(host + "Done transmitting");
            return false;
        }

        return _running;
    }
}
