package cs451.lat;

import cs451.network.SocketService;

import java.util.concurrent.atomic.AtomicInteger;

public class LATService
{
    public static final int SEND_PROPOSAL = 1;

    private static class Ack extends AtomicInteger
    {
        public Ack()
        {
            super( 0 );
        }

        public void reset()
        {
            set( 0 );
        }
    }


    public final Ack ack_count = new Ack();
    public final Ack nack_count = new Ack();
    public final AtomicInteger active_proposal_number = new AtomicInteger( 0 );

    public Proposal proposedValue = new Proposal();

    private final SocketService service;
    private final int nbHosts;

    public LATService( SocketService service )
    {
        this.service = service;
        this.nbHosts = service.getNbHosts();
    }

    private boolean notMajority( int acks )
    {
        return !(acks > (nbHosts / 2f));
    }

    // upon nack_count > 0 and ack_count + nack_count >= f+1
    public void checkProposalFinished( LATSender sender, int acks, int nacks )
    {
        if ( nacks <= 0 || notMajority( acks + nacks ) )
            return;

        sender.sendProposal();
    }

    // upon ack_count >= f+1
    public void checkDecide( int acks )
    {
        if ( notMajority( acks ) )
            return;

        service.registerProposal( proposedValue );
    }

    public void onAck( LATSender sender )
    {
        int acks = ack_count.incrementAndGet();
        int nacks = nack_count.get();
        checkProposalFinished( sender, acks, nacks );
        checkDecide( acks );
    }

    public void onNack( LATSender sender, Proposal value )
    {
        proposedValue.addAll( value );
        int acks = ack_count.get();
        int nacks = nack_count.incrementAndGet();
        checkProposalFinished( sender, acks, nacks );
    }

    public void resetAcks()
    {
        ack_count.reset();
        nack_count.reset();
    }
}
