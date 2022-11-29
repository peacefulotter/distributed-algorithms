package cs451.packet;

import cs451.lat.Proposal;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

public class SetMessage extends Message
{
    public final Proposal proposal;

    public SetMessage( PacketTypes type, int round, int prop_nb, int src, Proposal proposal )
    {
        super( type, round, prop_nb, src );
        this.proposal = proposal;

        if ( type.getPacketClass() != PacketClass.SET )
            throw new RuntimeException("SetPacket PacketClass not SET");
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        SetMessage that = (SetMessage) o;
        return super.equals( that ) &&  proposal.equals( that.proposal );
    }

    @Override
    public int hashCode()
    {
        int hash = super.hashCode();
        hash = 89 * hash + Objects.hash( proposal );
        return hash;
    }

    @Override
    public String toString()
    {
        return super.toString() + ", PRO=" + proposal;
    }

    @Override
    protected Comparator<Message> getComparator()
    {
        // TODO: compare proposal
        return super.getComparator();
    }

    public int getProposalSize() { return proposal.size(); }
    public Set<Integer> getProposal() { return proposal; }
}
