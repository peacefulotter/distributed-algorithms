package cs451.beb;

import cs451.Host;
import cs451.packet.Message;
import cs451.pl.PLSender;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.utils.Logger;
import cs451.utils.Sleeper;

public class BEBSender extends PLSender
{
    // TODO: 16Mb per process

    public BEBSender( SocketService service )
    {
        super( service );
    }

    public void broadcast( Message msg )
    {
        bebBroadcast( msg );
    }

    public void bebBroadcast( Message msg )
    {
        Packet packet;
        for ( Host dest : service.getHosts() )
        {
            packet = new Packet( msg, dest );
            pp2pBroadcast( packet );
            onBroadcast( packet );
        }
    }

    @Override
    public void run()
    {
        int nbHosts = service.getNbHosts();
        Message msg = Message.getFirst( service );
        while ( !service.closed.get() )
        {
            if ( !toBroadcast.isEmpty() )
            {
                Message m = toBroadcast.poll();
                bebBroadcast( m );
                Logger.log( "BEBSender","toBroadcast - Broadcasted packet " + m );
            }
            else if ( !toSend.isEmpty() )
            {
                Packet p = toSend.poll();
                pp2pBroadcast( p );
                Logger.log( "BEBSender","toSend - Sent packet " + p );
            }
            else if (
                packetsToSend.get() >= nbHosts &&
                msg.seq <= service.nbMessages
            )
            {
                broadcast( msg );
                Logger.log( "BEBSender","normal - Sent packet " + msg );
                packetsToSend.addAndGet( -nbHosts );
                msg = msg.getNext( service );
                onNewBroadcast( new Packet( msg, service.getSelf() ) );
            }
            Sleeper.release();
        }
    }
}