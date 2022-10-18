package cs451.packet;


public enum PacketTypes
{
    BROADCAST( 'b', (p) -> p.getSeqNr() + "" ),
    DELIVER( 'd', (p) -> p.getSender() + " " + p.getSeqNr() ),
    ACK( 'a', (p) -> "" ),
    NOTIFY( 'n', (p) -> "" );

    private interface PacketLambda
    {
        String apply( Packet packet );
    }

    private final char tag;
    private final PacketLambda lambda;

    PacketTypes( char tag, PacketLambda lambda )
    {
        this.tag = tag;
        this.lambda = lambda;
    }

    public String getFileLine( Packet packet )
    {
        return tag + " " + lambda.apply( packet );
    }

    public char getTag()
    {
        return tag;
    }

    @Override
    public String toString()
    {
        return tag + "";
    }
}
