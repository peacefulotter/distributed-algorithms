package cs451.packet;

import cs451.Host;
import cs451.lat.Proposal;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SetPacket extends Packet
{
    // TODO: remove origin

    public final Proposal proposal;

    public SetPacket( PacketTypes type, int seq, int origin, int src, int dest, Proposal proposal )
    {
        super(type, seq, origin, src, dest );
        this.proposal = proposal;
    }
    public SetPacket( SetMessage msg, Host dest )
    {
        super( msg, dest );
        this.proposal = msg.proposal;
    }

    private SetPacket( Packet p, Proposal proposal )
    {
        this( p.type, p.seq, p.origin, p.src, p.getDestId(), proposal);
    }

    public static SetPacket fromDatagram( ByteBuffer bb, PacketTypes type, DatagramPacket from, Host dest )
    {
        Packet p = Packet.fromDatagram( bb, type, from, dest );
        int nbProposals = bb.getInt();
        Proposal proposal = new Proposal();
        for ( int i = 0; i < nbProposals; i++ )
            proposal.add( bb.getInt() );
        return new SetPacket( p, proposal );
    }

    @Override
    public DatagramPacket getDatagram()
    {
        // TODO: separate packets int 8 msgs / packet
        // Message.BUFFER_CAPACITY + 4 + 4 * proposal.size()
        int capacity = Packet.BUFFER_CAPACITY + 4 * (getProposalSize() + 1);
        ByteBuffer bb = super.getPacketBuffer( capacity );
        bb.putInt( proposal.size() );
        proposal.forEach( bb::putInt );
        return new DatagramPacket( bb.array(), bb.capacity() );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        SetPacket that = (SetPacket) o;
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
