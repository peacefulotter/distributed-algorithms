package cs451.packet;


import java.util.List;
import java.util.stream.Collectors;

public enum PacketTypes
{
    BRC( 'b', ( p, i) -> i + "" ),
    ACK( 'd', (p, i) -> p.getSrc() + " " + i ),
    UNKNOWN('u', (p, i) -> "");

    private interface PacketLambda
    {
        String apply( Packet packet, int i );
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

    public List<String> getFileLines( Packet packet )
    {
        return packet.getSeqRange()
            .map( i -> tag + " " + lambda.apply( packet, i ) )
            .collect( Collectors.toList() );
    }

    public char getTag() { return tag; }

    @Override
    public String toString()
    {
        return tag + "";
    }
}
