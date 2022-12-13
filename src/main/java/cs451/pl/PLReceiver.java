package cs451.pl;

import cs451.network.SocketHandler;
import cs451.network.SocketService;
import cs451.packet.*;
import cs451.utils.Logger;
import cs451.utils.Pair;
import cs451.utils.Sleeper;
import jdk.jfr.DataAmount;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class PLReceiver extends SocketHandler
{
    // seq, src
    protected final Set<MiniPacket> delivered;

    protected PLSender sender;

    public PLReceiver( SocketService service )
    {
        super( service );
        this.delivered = new HashSet<>();
    }

    public void setSender( PLSender sender )
    {
        this.sender = sender;
    }


    public boolean deliver( GroupedPacket p )
    {
        MiniPacket mp = p.minify();
        return deliver( mp );
    }

    public boolean deliver( MiniPacket mp )
    {
        Logger.log(service.id, "Delivering: " + mp);
        return delivered.add( mp );
    }

    public void onPacket( GroupedPacket p ) {}

    private boolean isAck( DatagramPacket dp )
    {
        byte tag = dp.getData()[0];
        return tag == AckParser.ACK_TAG;
    }

    @Override
    public void run()
    {
        while ( !service.closed.get() )
        {
            DatagramPacket dp = service.getIncomingPacket();
            if ( isAck( dp ) )
            {
                MiniPacket p = AckParser.parse( dp ).revert();
                Logger.log(service.id, "PLReceiver", "Received ACK " + p);
                if ( deliver(p) )
                    sender.onAcknowledge( p );
            }
            else
            {
                GroupedPacket p = PacketParser.parse( dp, service.id );
                DatagramPacket res = AckParser.format( p );
                service.sendPacket( res, p.src );
                if ( deliver(p) )
                    onPacket( p );
            }

            Sleeper.release();
        }
    }
}
