package cs451.listener;

import cs451.packet.Packet;

import java.net.DatagramPacket;

public interface DeliverListener
{
    void actionPerformed( DatagramPacket dp, Packet packet );
}