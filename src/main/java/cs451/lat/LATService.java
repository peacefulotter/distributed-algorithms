package cs451.lat;

import java.util.concurrent.atomic.AtomicInteger;

public class LATService
{
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


    private final Ack ack_count = new Ack();
    private final Ack nack_count = new Ack();
    public final AtomicInteger active_proposal_number = new AtomicInteger( 0 );

    public boolean active = false;
    public Proposal proposedValue = new Proposal();

    private final int nbHosts;

    public LATService( int nbHosts )
    {
        this.nbHosts = nbHosts;
    }

    private boolean notMajority( int acks )
    {
        return !(acks > (nbHosts / 2f));
    }

    public void checkProposalFinished( int acks, int nacks )
    {
        if ( nacks <= 0 || notMajority( acks + nacks ) || !active )
            return;

        active_proposal_number.incrementAndGet();
        ack_count.reset();
        nack_count.reset();
        // ((LATSender) sender).sendProposal();
    }

    public void checkDecide( int acks )
    {
        if ( notMajority( acks ) || !active )
            return;

        // TODO: decide(proposed_value)
        active = false;
    }

    public void incAckCount()
    {
        int acks = ack_count.incrementAndGet();
        int nacks = nack_count.get();
        checkProposalFinished( acks, nacks );
        checkDecide( acks );
    }

    public void incNackCount()
    {
        int acks = ack_count.get();
        int nacks = nack_count.incrementAndGet();
        checkProposalFinished( acks, nacks );
    }
}
