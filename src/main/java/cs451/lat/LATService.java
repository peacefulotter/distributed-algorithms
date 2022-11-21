package cs451.lat;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class LATService
{
    private final Ack ack_count = new Ack();
    private final Ack nack_count = new Ack();

    public boolean active = false;
    public int active_proposal_number = 0;
    public Set<Integer> proposedValue = new HashSet<>();

    private final int nbHosts;

    private static class Ack
    {
        private final AtomicInteger ack = new AtomicInteger(0);
        public int incrementAndGet() { return ack.incrementAndGet(); }
        public int get() { return ack.get(); }
        public void reset() { ack.set( 0 ); }

    }

    public LATService( int nbHosts )
    {
        this.nbHosts = nbHosts;
    }

    private boolean notMajority( int acks)
    {
        return !(acks > (nbHosts / 2f));
    }

    public boolean checkProposalFinished( int acks, int nacks )
    {
        if ( nacks <= 0 || notMajority( acks + nacks ) || !active )
            return false;

        active_proposal_number++;
        ack_count.reset();
        nack_count.reset();
        // ((LATSender) sender).sendProposal();
        return true;
    }

    public void checkDecide(int acks)
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
        checkDecide(acks);
    }

    public void incNackCount()
    {
        int acks = ack_count.get();
        int nacks = nack_count.incrementAndGet();
        checkProposalFinished( acks, nacks );
    }
}
