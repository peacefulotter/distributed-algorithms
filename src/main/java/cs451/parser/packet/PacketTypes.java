package cs451.parser.packet;


public enum PacketTypes
{
    BROADCAST('b'),
    DELIVER('d'),
    ACK('a'),
    NOTIFY('n');

    private final char tag;

    PacketTypes( char tag )
    {
        this.tag = tag;
    }

    public char getTag()
    {
        return tag;
    }
}
