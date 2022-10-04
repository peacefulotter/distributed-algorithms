package cs451.parser.perfectlink;

import cs451.Host;
import cs451.parser.packet.PLPacket;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

abstract public class Server extends Thread
{
    protected final DatagramSocket socket;
    protected final FileHandler handler;
    protected final Host host;
    protected final Host dest;

    protected final Set<String> delivered;

    private final byte[] buf = new byte[256];

    public Server( Host host, Host dest, String output )
    {
        this.host = host;
        this.dest = dest;
        this.delivered = new HashSet<>();
        this.handler = new FileHandler( output );

        try
        {
            this.socket = new DatagramSocket( host.getAddress() );
            if ( host.getId() != dest.getId() )
            {
                System.out.println("Binding " + host + " to " + dest);
                this.socket.connect( dest.getAddress() );
            }
        } catch ( SocketException e )
        {
            throw new RuntimeException( e );
        }

        System.out.println( host + "Socket connected" );
    }

    protected DatagramPacket getIncomingPacket()
    {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try { socket.receive(packet); }
        catch ( IOException e ) { throw new RuntimeException( e ); }
        return packet;
    }

    protected String parsePacket( DatagramPacket packet )
    {
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        return new String(packet.getData(), 0, packet.getLength());
    }

    protected boolean sendPacket( PLPacket packet )
    {
        System.out.println(host + "Sending packet: " + packet);
        try {
            socket.send( packet.getDatagram() );
            handler.write( packet.getMsg() );
        }
        catch ( IOException e ) {
            System.out.println( host + e.getMessage() );
            return false;
        }

        return true;
    }


    abstract protected boolean _run();

    @Override
    public void run()
    {
        boolean running = true;
        System.out.println(host + "Socket running...");

        while (running)
            running = _run();

        closeConnection();
    }


    public void closeConnection() {
        System.out.println( host + "Closing connection" );
        socket.close();
    }
}
