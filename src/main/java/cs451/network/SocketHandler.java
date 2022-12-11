package cs451.network;


import cs451.Host;
import cs451.packet.GroupedPacket;

public abstract class SocketHandler implements Runnable
{
    protected SocketService service;

    public SocketHandler( SocketService service )
    {
        this.service = service;
    }

    protected boolean sendPacket( GroupedPacket packet )
    {
        final Host dest = Host.get( packet.dest );
        return service.sendPacket( packet, dg -> {
            dg.setAddress( dest.getAddress() );
            dg.setPort( dest.getPort() );
        } );
    }
}
