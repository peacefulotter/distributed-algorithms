package cs451.beb;

import cs451.Host;
import cs451.packet.Message;
import cs451.pl.PLSender;
import cs451.network.SocketService;
import cs451.packet.Packet;
import cs451.utils.Logger;
import cs451.utils.Sleeper;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BEBSender extends PLSender
{
    // TODO: 16Mb per process

    private final ConcurrentLinkedQueue<Message> messages;

    public BEBSender( SocketService service )
    {
        super( service );
        this.messages = new ConcurrentLinkedQueue<>();
    }

    public boolean broadcast( Message msg )
    {
        return bebBroadcast( msg );
    }

    public boolean bebBroadcast( Message msg )
    {
        boolean sent = true;
        Packet packet = null;
        for ( Host dest : service.getHosts() )
        {
            packet = new Packet( msg, dest );
            boolean broadcasted = pp2pBroadcast( packet );
            sent = sent && broadcasted;
        }

        // if successfully sent and it's not a relay
        if ( sent && packet != null && packet.getOrigin() == packet.getSrc() )
            super.onNewBroadcast( packet );

        return sent;
    }

    // Override it to avoid registering multiple time the same broadcast packet
    // instead call super.onNewBroadcast ONCE at the end of bebBroadcast
    // to register the broadcast packet once
    @Override
    protected void onNewBroadcast( Packet packet )
    {
        // if origin == src -> broadcast registered by bebBroadcast
        // else -> don't register the broadcast as it is a relay
    }

    public void addMessageQueue( Message msg )
    {
        this.messages.add( msg );
    }

    @Override
    public void run()
    {
        int nbHosts = service.getNbHosts();
        Message msg = Message.getFirst( service );
        while ( !service.closed.get() )
        {
            Logger.log( "BEBSender", messages );
            Message fromQueue = messages.poll();
            if ( fromQueue != null )
                bebBroadcast( fromQueue ); // TODO: later - not BEB

            else if (
                packetsToSend.get() >= nbHosts &&
                msg.seq <= service.nbMessages &&
                broadcast( msg )
            )
            {
                packetsToSend.addAndGet( -nbHosts );
                msg = msg.getNext( service );
            }
            Sleeper.release();
        }
        Logger.log( "BEBSender", " Done" );
    }
}

