package cs451.packet;


// TODO: remove packet class
public enum PacketTypes
{
    BRC( 'b' ),
    ACK( 'd' ),
    LAT_PROP('p' ),
    LAT_ACK('a' ),
    LAT_NACK( 'n' );

    private final char tag;

    PacketTypes( char tag )
    {
        this.tag = tag;
    }

    public static PacketTypes parseType( char tag )
    {
        for ( PacketTypes type: values() )
            if ( type.getTag() == tag )
                return type;
        return null;
    }

    public char getTag() { return tag; }

    @Override
    public String toString()
    {
        return tag + "";
    }
}
