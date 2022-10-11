package cs451.parser.perfectlink;

public class Timeout
{
    private static final int MULT = 2;
    private static final int MAX_INCREASE = 10;
    private static final int MIN = 50;
    private static final int MAX = MIN * (int) Math.pow( 2, MAX_INCREASE );

    private int timeout = MIN;

    public void increase()
    {
        timeout =  Math.min( timeout * MULT, MAX );
        System.out.println("Increasing timeout " + timeout);
    }

    public void decrease()
    {
        timeout = Math.max( timeout / MULT, MIN );
        System.out.println("Decreasing timeout " + timeout);
    }

    public int get() { return timeout; }
}
