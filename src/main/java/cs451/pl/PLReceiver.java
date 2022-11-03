package cs451.network;

import cs451.packet.Packet;
import cs451.packet.PacketTypes;
import cs451.utils.Logger;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import static cs451.utils.Logger.log;

public class Receiver extends Thread
{
    private final SocketService service;
    protected final List<Packet> delivered;

    private Pool pool;

    public Receiver( SocketService service )
    {
        this.service = service;
        this.delivered = new ArrayList<>();
    }

    public void setPool( Pool pool )
    {
        this.pool = pool;
    }

    protected void sendAck( DatagramPacket dp, Packet packet )
    {
        Packet ack = new Packet( PacketTypes.ACK, packet );
        log( "Sending ACK : " + ack );
        service.sendPacket( ack, dg -> {
            dg.setAddress( dp.getAddress() );
            dg.setPort( dp.getPort() );
        } );
    }

    private boolean deliver( Packet packet )
    {
        if ( delivered.contains( packet ) )
            return false;

        log( "Delivering : " + packet );
        listener.trigger( ack );
        delivered.add( packet );
        pool.register( packet );
        service.timeout.decrease();
        return true;
    }

    @Override
    public void run()
    {
        while ( pool.running )
        {
            // TODO
            // if ack => dispatch to sender
            // if broadcast => send ack + getIncomingPacket(timeout - timeToGetBroadcast)
            DatagramPacket packet = service.getIncomingPacket();
            Packet bc = new Packet( packet );
            log( "Received : " + bc );

            if ( bc.getType() == PacketTypes.BROADCAST )
                sendAck( packet, bc );
            else if ( bc.getType() == PacketTypes.ACK )
                deliver( bc );
        }
    }
}
