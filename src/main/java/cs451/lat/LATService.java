package cs451.lat;

import cs451.network.SocketService;
import cs451.utils.Logger;

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

    public final Ack ack_count = new Ack();
    public final Ack nack_count = new Ack(); // TODO: to AtomicBoolean
    public final AtomicInteger active_proposal_number = new AtomicInteger( 0 );

    public volatile Proposal proposed_value = new Proposal();
    public Proposal accepted_value = new Proposal();

    private final SocketService service;
    private final int f;
    public final int round;

    public LATService( SocketService service, int round )
    {
        this.service = service;
        this.f = (int) Math.floor( service.getNbHosts() / 2f );
        this.round = round;
    }

    private boolean notMajority( int acks )
    {
        return !( acks >= f + 1 );
    }

    // upon nack_count > 0 and ack_count + nack_count >= f+1
    public void checkProposalFinished( LATSender sender, int acks, int nacks )
    {
        Logger.log(service.id, "LATService  round=" + round,"Check proposal " + this);
        if ( nacks <= 0 || notMajority( acks + nacks ) )
            return;

        sender.sendProposal( this );
    }

    // upon ack_count >= f+1
    public void checkDecide( LATSender sender, LATReceiver receiver, int acks )
    {
        if ( notMajority( acks ) )
            return;

        Logger.log(service.id, "LATService  round=" + round, "///// DECIDING apn=" + active_proposal_number.get() + " ////// " + proposed_value );

        // register decision
        service.registerProposal( round, proposed_value );
        // add decided lat to receiver
        receiver.setDecided( round, proposed_value );
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

    // TODO: can decide if nack = n
    // n = ??
    public void onNack( LATSender sender, Proposal value )
    {
        proposed_value.addAll( value );
        int acks = ack_count.get();
        int nacks = nack_count.incrementAndGet();
        checkProposalFinished( sender, acks, nacks );
    }

    public void resetAcks()
    {
        ack_count.reset();
        nack_count.reset();
    }

    // TODO: delete?
    private void resetLatService()
    {
        resetAcks();
        proposed_value.clear();
        accepted_value.clear();
    }

    @Override
    public String toString()
    {
        return "{" +
            "f=" + f +
            ", round=" + round +
            ", ack_count=" + ack_count +
            ", nack_count=" + nack_count +
            ", active_prop_nb=" + active_proposal_number +
            ", proposed_value=" + proposed_value +
            '}';
    }
}
