package cs451.network;

import cs451.Host;
import cs451.packet.Packet;

import java.util.List;

public class BEBSender extends Sender
{
    private final PLSender pp2p;
    private final List<Host> hosts;

    public BEBSender( SocketService service, int nbMessages, List<Host> hosts )
    {
        super( service, nbMessages );
        this.pp2p = new PLSender( service, nbMessages );
        this.hosts = hosts;
    }

    @Override
    public boolean broadcast( Host dest, int seqNr, int messages )
    {
        return pp2p.broadcast( dest, seqNr, messages );
    }

    public boolean broadcast()
    {
        for ( Host dest: hosts )
            broadcast( dest, seqNr, messages );
        return true; // TODO: return product of broadcast returns
    }

    @Override
    public void run()
    {
        pp2p.run();
    }

    @Override
    public void onAck( Packet ack )
    {
        if ( acknowledged.incrementAndGet() == hosts.size() )
            done = true;
    }
}
