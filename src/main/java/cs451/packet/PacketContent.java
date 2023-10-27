package cs451.packet;

import cs451.lat.Proposal;

import java.util.Objects;

public class PacketContent
{
    private final PacketTypes type;
    private final Proposal prop;
    private final int round, prop_nb;

    public PacketContent( PacketTypes type, int round, int prop_nb, Proposal prop )
    {
        this.type = type;
        this.prop = prop;
        this.round = round;
        this.prop_nb = prop_nb;
    }

    public PacketContent( PacketTypes type, int round, int prop_nb)
    {
        this( type, round, prop_nb, new Proposal() );
    }

    public String string()
    {
        return "{ " +
            "type=" + type +
            ", round=" + round +
            ", prop_nb=" + prop_nb +
            ", prop=" + prop +
            " }";
    }
    @Override
    public String toString()
    {
        return "\n\t" + string();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        PacketContent that = (PacketContent) o;
        return round == that.round &&
            prop_nb == that.prop_nb &&
            type == that.type &&
            Objects.equals( prop, that.prop );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( type, prop, round, prop_nb );
    }

    public PacketTypes getType()
    {
        return type;
    }

    public Proposal getProposal()
    {
        return prop;
    }

    public int getRound()
    {
        return round;
    }

    public int getProp_nb()
    {
        return prop_nb;
    }
}
