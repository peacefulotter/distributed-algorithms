package cs451.parser.perfectlink;

import cs451.Host;
import cs451.parser.packet.Packet;
import cs451.parser.packet.PacketTypes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;

public class Sender extends Server
{
    private static final int MAX_RETRANSMIT_TRIES = 5;

    private final int nbMessages;
    private int seqNr;

    private int retransmitTries;
    private boolean receiverAlive;

    public Sender( Host host, Host dest, String output, PLConfig config )
    {
        super( host, output );
        this.nbMessages = config.getM();
        this.seqNr = 1;
        this.retransmitTries = 0;
        this.receiverAlive = false;

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
        catch ( PortUnreachableException | ClosedChannelException ignored ) {
            System.out.println(host + "PORT UNREACHABLE OR CHANNEL CLOSED");
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
        System.out.println(host + "Retransmit tries: " + retransmitTries + " / " + MAX_RETRANSMIT_TRIES);
        if ( retransmitTries++ >= MAX_RETRANSMIT_TRIES )
            terminate();
    }

    private boolean waitForAck()
    {
        // set timeout
        try { socket.setSoTimeout( timeout.get() ); }
        catch ( SocketException e ) { terminate( e ); }

        DatagramPacket packet = getIncomingPacket();

        // didn't receive ack in the timeout
        if ( packet == null )
        {
            System.out.println(host + "Failed to receive ACK for seq_nr: " + seqNr);
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
        if ( !delivered.contains( ackMsg ) )
        {
            seqNr++;
            retransmitTries = 0;
            delivered.add( ackMsg );
            timeout.decrease();
        }
    }

    private boolean broadcastAndAck()
    {
        // broadcast packet to receiver
        Packet bc = broadcastPacket();
        // if broadcast failed
        if ( bc == null )
            return false;

        // write broadcast msg
        handler.register( bc );
        // wait for ack
        return waitForAck();
    }

    @Override
    protected boolean runCallback()
    {
        boolean _running = true;

        if ( seqNr < nbMessages )
        {
            boolean broadcasted = broadcastAndAck();
            System.out.println(host + "BROADCASTED " + broadcasted + "SEQ_NR: " + seqNr) ;
            if ( !broadcasted )
            {
                prepareRetransmit();
                _running = retransmitTries < MAX_RETRANSMIT_TRIES;
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
