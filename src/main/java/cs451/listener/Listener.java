package cs451.listener;


import cs451.packet.Packet;

import java.net.DatagramPacket;

public class Listener
{
    private DeliverListener listener;

    public void trigger( DatagramPacket dp, Packet packet )
    {
        if ( listener == null ) return;
        listener.actionPerformed( dp, packet );
    }

    public void setListener( DeliverListener listener )
    {
        this.listener = listener;
    }
}