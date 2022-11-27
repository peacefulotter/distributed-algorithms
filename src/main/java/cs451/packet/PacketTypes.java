package cs451.packet;


import cs451.Host;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

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

    public static Packet parseDatagram( DatagramPacket dp, Host host )
    {
        ByteBuffer buffer = Packet.getBuffer( dp );
        PacketTypes type = parseType( buffer.getChar() );
        System.out.println(type);
        if ( type.packetClass == PacketClass.NORMAL )
            return Packet.fromDatagram( buffer, type, dp, host );
        else if ( type.packetClass == PacketClass.SET )
            return SetPacket.fromDatagram( buffer, type, dp, host );
        return null;
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
