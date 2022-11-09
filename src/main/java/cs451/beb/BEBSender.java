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
    private final ConcurrentLinkedQueue<Message> messages;
    private final ConcurrentLinkedQueue<Integer> seqBroadcasted;

    public BEBSender( SocketService service )
    {
        super( service );
        this.messages = new ConcurrentLinkedQueue<>();
        this.seqBroadcasted = new ConcurrentLinkedQueue<>();
    }

    public boolean broadcast( Message msg )
    {
        return bebBroadcast( msg );
    }

    public boolean bebBroadcast( Message msg )
    {
        boolean sent = true;
        for ( Host dest : service.getHosts() )
        {
            Packet packet = new Packet( msg, dest );
            boolean broadcasted = pp2pBroadcast( packet );
            sent = sent && broadcasted;
        }
        return sent;
    }

    @Override
    protected void onBroadcast( Packet packet )
    {
        int seq = packet.getSeqNr();
        if ( !seqBroadcasted.contains( seq ) )
        {
            seqBroadcasted.add( seq );
            service.register( packet );
        }
    }

    public void addMessageQueue( Message msg )
    {
        this.messages.add( msg );
    }

    @Override
    public void run()
    {
        Message msg = Message.getFirst( service );
        while ( !service.closed.get() )
        {
            Logger.log( messages );
            Message fromQueue = messages.poll();
            if ( fromQueue != null )
                bebBroadcast( fromQueue );

            else if ( packetsToSend.get() > 0 && msg.seq <= service.nbMessages )
            {
                if ( broadcast( msg ) )
                    packetsToSend.decrementAndGet();
                msg = msg.getNext( service );
            }
            Sleeper.release();
        }
        Logger.log( "BEBSender", " Done" );
    }
}

