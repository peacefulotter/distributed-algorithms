package cs451.network;


import cs451.packet.Packet;

public abstract class SocketHandler implements Runnable
{
    protected SocketService service;

    public SocketHandler( SocketService service )
    {
        this.service = service;
    }

    protected boolean sendPacket( Packet packet )
    {
        return service.sendPacket( packet, dg -> {
            dg.setAddress( packet.getDestAddress() );
            dg.setPort( packet.getDestPort() );
        } );
    }
}
