package cs451.packet;


import java.util.List;
import java.util.stream.Collectors;

public enum PacketTypes
{
    BRC( 'b', ( p, i) -> i + "" ),
    ACK( 'd', (p, i) -> p.getSrc() + " " + i ),
    LAT_PRO('p', (p, i) -> "" ),
    LAT_ACK('a', (p, i) -> "" ),
    LAT_NACK( 'n', (p, i) -> ""),
    UNKNOWN('u', (p, i) -> "");

    private interface PacketLambda
    {
        String apply( Message m, int i );
    }

    private final char tag;
    private final PacketLambda lambda;

    PacketTypes( char tag, PacketLambda lambda )
    {
        this.tag = tag;
        this.lambda = lambda;
    }

    public static PacketTypes parseType( char tag )
    {
        for ( PacketTypes type: values() )
            if ( type.getTag() == tag )
                return type;
        return UNKNOWN;
    }

    public List<String> getFileLines( Message m )
    {
        return m.getSeqRange()
            .map( i -> tag + " " + lambda.apply( m, i ) )
            .collect( Collectors.toList() );
    }

    public char getTag() { return tag; }

    @Override
    public String toString()
    {
        return tag + "";
    }
}
