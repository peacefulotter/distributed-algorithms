package cs451.packet;


import java.util.List;
import java.util.stream.Collectors;

public enum PacketTypes
{
    BROADCAST( 'b', (p, i) -> i + "" ),
    DELIVER( 'd', (p, i) -> p.getSender() + " " + i ),
    ACK( 'a', (p, i) -> "" );

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

    public List<String> getFileLines( Packet packet )
    {
        return packet.getSeqRange()
            .map( i -> tag + " " + lambda.apply( packet, i ) )
            .collect( Collectors.toList() );
    }

    @Override
    public String toString()
    {
        return tag + "";
    }
}
