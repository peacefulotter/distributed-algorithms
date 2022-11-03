package cs451.network;

import cs451.Host;
import cs451.packet.PacketTypes;
import cs451.parser.HostsParser;
import cs451.utils.Logger;
import cs451.packet.Packet;
import cs451.utils.Sleeper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PLSender extends Sender
{
    private static final String prefix = "PLSender";

    protected final ConcurrentMap<Packet, Integer> broadcasted;

    public PLSender( SocketService service, int nbMessages )
    {
        super( service, nbMessages );
        this.broadcasted = new ConcurrentHashMap<>();
    }

    public boolean broadcast( Host dest, int seqNr, int messages )
    {
        Packet packet = new Packet( PacketTypes.BROADCAST, seqNr, service.id, dest.getId(), messages );
        boolean sent = service.sendPacket( packet, dg -> {
            dg.setAddress( dest.getAddress() );
            dg.setPort( dest.getPort() );
        } );
        if ( sent )
            broadcasted.put( packet, service.timeout.get() );
        Logger.log( prefix, "Sent packet " + packet + " to " + dest);
        return sent;
    }

    public void onAck( Packet packet )
    {
        /*Long time = broadcasted.get( packet );
        if ( time == null )
        {
            broadcasted.replaceAll( (p, l) -> timeout - System.nanotime().toMS() );
        }*/
    }

    public void run( Host dest )
    {
        while ( !service.closed && !queue.isEmpty() )
        {
            SeqMsg sm = queue.poll();
            broadcast( dest, sm.seqNr, sm.messages );
            Sleeper.release();
        }
        Logger.log(prefix, " Done");
    }

}
