package cs451.lat;

import cs451.network.SocketService;
import cs451.packet.PacketContent;
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
    public final Ack nack_count = new Ack();
    public final AtomicInteger active_proposal_number = new AtomicInteger( 0 );
    private final SocketService service;
    private final int majority;

    public volatile Proposal proposed_value = new Proposal();
    public final int round;

    public LATService( SocketService service, int round )
    {
        this.service = service;
        this.majority = (int) Math.floor( service.getNbHosts() / 2f ) + 1;
        this.round = round;
    }

    private boolean notMajority( int acks )
    {
        return acks < majority;
    }

    // upon nack_count > 0 and ack_count + nack_count >= majority
    public void checkProposalFinished( LATSender sender, int acks, int nacks )
    {
        Logger.log(service.id, "LATService  round=" + round,"Check proposal " + this);
        if ( nacks <= 0 || notMajority( acks + nacks ) )
            return;

        ack_count.reset();
        nack_count.reset();
        int apn = active_proposal_number.incrementAndGet();
        sender.sendProposal( this, apn );
    }


    public void decide(LATSender sender, LATReceiver receiver, Proposal decision )
    {
        Logger.print(service.id, "LATService  round=" + round, "///// DECIDING apn=" + active_proposal_number.get() + " ////// " + decision );

        // register decision
        service.registerProposal( round, decision );
        // add decided lat to receiver
        receiver.setDecided( round, decision );
        // move to next round -> propose new proposal
        sender.moveNextProposal();
    }

    // upon ack_count >= majority+1
    public void checkDecide( LATSender sender, LATReceiver receiver, int acks )
    {
        if ( notMajority( acks ) )
            return;

        decide(sender, receiver, proposed_value);
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
        proposed_value.addAll( value );
        int acks = ack_count.get();
        int nacks = nack_count.incrementAndGet();
        checkProposalFinished( sender, acks, nacks );
    }

    @Override
    public String toString()
    {
        return "{" +
            "majority=" + majority +
            ", round=" + round +
            ", ack_count=" + ack_count +
            ", nack_count=" + nack_count +
            ", active_prop_nb=" + active_proposal_number +
            ", proposed_value=" + proposed_value +
            '}';
    }
}
