package cs451.beb;

import cs451.Host;
import cs451.packet.Message;
import cs451.packet.SetMessage;
import cs451.pl.PLSender;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.utils.Logger;
import cs451.utils.Sleeper;

import java.util.Set;

abstract public class BEBSender extends PLSender
{
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
        }
    }

    abstract public SetMessage propose( Set<Integer> proposal );

    @Override
    public void run()
    {
        int seq = 1;
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
                proposalsToSend.get() > 0 &&
                !service.proposals.isEmpty()
            )
            {
                System.out.println( "Proposals to send: " + proposalsToSend.decrementAndGet() );
                Set<Integer> proposal = service.proposals.poll();
                // SetMessage msg = new SetMessage( proposal, seq, service.id );
                SetMessage msg = propose( proposal );
                seq++;
                Logger.log( "BEBSender","normal - Sent packet " + msg );
            }
            Sleeper.release();
        }
    }
}