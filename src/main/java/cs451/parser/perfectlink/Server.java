package cs451.parser.perfectlink;

import cs451.Host;
import cs451.parser.packet.BroadcastPacket;
import cs451.parser.packet.DeliveryPacket;
import cs451.parser.packet.Packet;

import java.io.IOException;
import java.net.*;

public class Server extends Thread
{
    private final DatagramSocket socket;
    private final Host host;

    private byte[] buf = new byte[256];
    private int seqNr;

    public Server( Host host )
    {
        this.host = host;
        this.seqNr = 1;

        try
        {
            this.socket = new DatagramSocket();
            this.socket.connect( new InetSocketAddress(host.getIp(), host.getPort()) );
        } catch ( SocketException e )
        {
            System.out.println(e);
            throw new RuntimeException( e );
        }

        System.out.println( getFullName() + "Socket connected" );
    }

    private DatagramPacket getIncomingPacket()
    {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try { socket.receive(packet); }
        catch ( IOException e ) { throw new RuntimeException( e ); }
        return packet;
    }

    private String parsePacket( DatagramPacket packet )
    {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        return new String(packet.getData(), 0, packet.getLength());
    }

    private void sendPacket( Packet packet )
    {
        try { socket.send(packet.getDatagram()); }
        catch ( IOException e ) { throw new RuntimeException( e ); }
    }

    public void run()
    {
        boolean running = true;
        System.out.println(getFullName() + "Socket running...");

        while (running)
        {
            if ( host.getId() == 0 )
            {
                DatagramPacket packet = getIncomingPacket();
                String msg = parsePacket( packet );
                System.out.println( getFullName() + "Received from: " + packet.getPort() + ", msg: " + msg );
                DeliveryPacket deliverPacket = new DeliveryPacket( packet.getData() );
                System.out.println(deliverPacket);
            }
            else if ( seqNr < 10 )
            {
                Packet broadcastPacket = getBroadcastPacket();
                sendPacket( broadcastPacket );
            }
            else
            {
                System.out.println(getFullName() + " Done transmitting");
                running = false;
            }
        }

        closeConnection();
    }

    public void closeConnection() {
        System.out.println( getFullName() + "Closing connection" );
        socket.close();
    }

    private Packet getDeliveryPacket()
    {
        return new DeliveryPacket( seqNr++, host.getId() );
    }

    private Packet getBroadcastPacket()
    {
        return new BroadcastPacket( seqNr++ );
    }

    private String getFullName()
    {
        return "[" + host.getIp() + ":" + host.getPort() + "] ";
    }
}
