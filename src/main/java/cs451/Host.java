package cs451;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Host {

    private static final String IP_START_REGEX = "/";
    public static final Map<Integer, Integer> portToId = new HashMap<>();

    private final int id, port;
    private final String ip;
    private final InetSocketAddress socketAddress;

    public Host( int id, int port, String ip )
    {
        this.id = id;
        this.port = port;
        this.ip = ip;
        this.socketAddress = new InetSocketAddress( ip, port );
    }

    public static Host populate( String idString, String ipString, String portString) throws Exception
    {
        int id, port = 0;
        String ip;

        try {
            id = Integer.parseInt(idString);

            String ipTest = InetAddress.getByName(ipString).toString();
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = ipTest.substring(1);
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]).getHostAddress();
            }

            port = Integer.parseInt(portString);
            if (port <= 0) {
                throw new Exception("Port in the hosts file must be a positive number!");
            }
        } catch (NumberFormatException e)
        {
            if ( port == -1 )
            {
                throw new Exception( "Id in the hosts file must be a number!" );
            } else
            {
                throw new Exception( "Port in the hosts file must be a number!" );
            }
        }

        portToId.put( port, id );

        return new Host(id, port, ip);
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString()
    {
        return "(" + id + ") - " + ip + ":" + port + " ";
    }

    public InetSocketAddress getSocketAddress() { return socketAddress; }
    public InetAddress getAddress() { return socketAddress.getAddress(); }

}
