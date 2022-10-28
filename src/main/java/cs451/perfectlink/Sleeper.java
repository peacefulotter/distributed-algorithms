package cs451.perfectlink;

public class Sleeper
{
    public static void sleep(long ms)
    {
        try
        {
            Thread.sleep( ms );
        } catch ( InterruptedException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static void release()
    {
        sleep(1);
    }
}
