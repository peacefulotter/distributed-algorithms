package cs451.parser.perfectlink;

import cs451.Host;

public class PLHost extends Host
{
    private int m, i;
    private Server server;

    public void setParams(int m, int i)
    {
        this.m = m;
        this.i = i;
        this.server = new Server( this );
        Thread thread = new Thread( server );
        thread.start();
    }

    public void closeConnection() { server.closeConnection(); }
}
