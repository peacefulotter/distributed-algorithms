package cs451.lat;

import cs451.network.SocketService;
import cs451.utils.Logger;

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
    private final int f;

    public LATService( SocketService service )
    {
        this.service = service;
        this.f = (int) Math.floor( service.getNbHosts() / 2f );
    }

    private boolean notMajority( int acks )
    {
        return !( acks >= f + 1 );
    }

    // upon nack_count > 0 and ack_count + nack_count >= f+1
    public void checkProposalFinished( LATSender sender, int acks, int nacks )
    {
        Logger.log(service.id, "LATService","Check proposal " + this);
        if ( nacks <= 0 || notMajority( acks + nacks ) )
            return;

        sender.sendProposal();
    }

    // upon ack_count >= f+1
    public void checkDecide( LATSender sender, LATReceiver receiver, int acks )
    {
        if ( notMajority( acks ) )
            return;

        Logger.log(service.id, "LATService", "DECIDING == " + proposedValue);

        // register decision
        service.registerProposal( proposedValue );
        // reset receiver and lat service
        receiver.reset();
        resetLatService();
        // move to next round -> propose new proposal
        sender.moveNextProposal();
    }

    public void onAck( LATSender sender, LATReceiver receiver )
    {
        int acks = ack_count.incrementAndGet();
        int nacks = nack_count.get();
        checkProposalFinished( sender, acks, nacks );
        checkDecide( sender, receiver, acks );
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

    private void resetLatService()
    {
        resetAcks();
        proposedValue.clear();
    }

    @Override
    public String toString()
    {
        return "{" +
            "f=" + f +
            ", ack_count=" + ack_count +
            ", nack_count=" + nack_count +
            ", active_prop_nb=" + active_proposal_number +
            ", proposedValue=" + proposedValue +
            '}';
    }
}
