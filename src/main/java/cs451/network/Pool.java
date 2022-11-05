package cs451.network;

import cs451.pl.PLReceiver;
import cs451.pl.PLSender;

public class Pool
{
    private final PLSender sender;
    private final PLReceiver receiver;

    private Pool( PLSender sender, PLReceiver receiver )
    {
        this.sender = sender;
        this.receiver = receiver;
    }

    public static Pool getPool( PLSender sender, PLReceiver receiver )
    {
        sender.setReceiver( receiver );
        receiver.setSender( sender );
        return new Pool( sender, receiver );
    }

    public void start()
    {
        new Thread( sender ).start();
        new Thread( receiver ).start();
    }
}
