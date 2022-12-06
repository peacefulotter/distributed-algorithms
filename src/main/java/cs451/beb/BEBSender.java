package cs451.beb;

import cs451.Host;
import cs451.lat.Proposal;
import cs451.packet.Message;
import cs451.packet.SetMessage;
import cs451.packet.SetPacket;
import cs451.pl.PLSender;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.utils.Logger;
import cs451.utils.Sleeper;
import cs451.utils.Stopwatch;

abstract public class BEBSender extends PLSender
{
    public BEBSender( SocketService service )
    {
        super( service );
        Stopwatch.init();
    }

    public void broadcast( Message msg )
    {
        bebBroadcast( msg );
    }

    protected void bebBroadcast( Message msg )
    {
        Packet packet;
        for ( Host dest : service.getHosts() )
        {
            packet = new Packet( msg, dest );
            pp2pBroadcast( packet );
        }
    }

    protected void bebBroadcastSet( SetMessage msg )
    {
        Logger.log(service.id, "BEBSender", "Broadcasting msg: " + msg);
        Packet packet;
        for ( Host dest : service.getHosts() )
        {
            packet = new SetPacket( msg, dest );
            pp2pBroadcast( packet );
        }
    }

    abstract public SetMessage propose( int round, Proposal proposal );

    @Override
    public void run()
    {
        int round = 0;
        while ( !service.closed.get() )
        {
            if ( !toBroadcast.isEmpty() )
            {
                SetMessage m = toBroadcast.poll();
                bebBroadcastSet( m );
                Logger.log( "BEBSender","toBroadcast - Broadcasted packet " + m );
            }
            else if ( !toSend.isEmpty() )
            {
                Packet p = toSend.poll();
                pp2pBroadcast( p );
                Logger.log( "BEBSender","toSend - Sent packet " + p );
            }
            else if (
                !service.proposals.isEmpty() &&
                proposalsToSend.get() > 0
            )
            {
                Proposal proposal = service.proposals.poll();
                // SetMessage msg = new SetMessage( proposal, seq, service.id );
                SetMessage msg = propose( round, proposal );
                round++;
                Logger.log( "BEBSender","normal - Sent packet " + msg );
                if (service.proposals.isEmpty())
                    Stopwatch.stop();
            }
            Sleeper.release();
        }
    }
}