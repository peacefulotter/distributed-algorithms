package cs451.network;

public abstract class SocketHandler implements Runnable
{
    protected SocketService service;

    public SocketHandler( SocketService service )
    {
        this.service = service;
    }
}
