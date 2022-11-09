package cs451.fifo;

import cs451.packet.Packet;

import java.util.*;

public class OrderPreserver
{
    public int lastResolved = 0;
    // seqIndex -> Packet
    public final Map<Integer, Packet> pending = new HashMap<>();

    @Override
    public String toString()
    {
        return "(" +
            "lastResolved=" + lastResolved +
            ", pending=" + pending +
            ")\n";
    }
}
