package cs451.packet;


// TODO: remove packet class
public enum PacketTypes
{
    BRC( 'b', PacketClass.NORMAL ),
    ACK( 'd', PacketClass.NORMAL ),
    LAT_PROP('p', PacketClass.SET ),
    LAT_ACK('a', PacketClass.NORMAL ),
    LAT_NACK( 'n', PacketClass.SET ),
    UNKNOWN('u', PacketClass.UNKNOWN );

    private final char tag;
    private final PacketClass packetClass;

    PacketTypes( char tag, PacketClass packetClass )
    {
        this.tag = tag;
        this.packetClass = packetClass;
    }

    public static PacketTypes parseType( char tag )
    {
        for ( PacketTypes type: values() )
            if ( type.getTag() == tag )
                return type;
        return UNKNOWN;
    }

    public char getTag() { return tag; }

    public PacketClass getPacketClass()
    {
        return packetClass;
    }

    @Override
    public String toString()
    {
        return tag + "";
    }
}
