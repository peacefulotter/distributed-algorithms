package cs451.perfectlink;

public class Timeout
{
    private static final float MULT_INCREASE = 2f;
    private static final float MULT_DECREASE = 0.75f;
    private static final int MAX_INCREASE = 10;
    private static final int MIN = 50;
    private static final int MAX = MIN * (int) Math.pow( 2, MAX_INCREASE );

    private int timeout = MIN;

    public void increase()
    {
        timeout = (int) Math.min( timeout * MULT_INCREASE, MAX );
        System.out.println("Increasing timeout " + timeout);
    }

    public void decrease()
    {
        timeout = (int) Math.max( timeout * MULT_DECREASE, MIN );
        System.out.println("Decreasing timeout " + timeout);
    }

    public int get() { return timeout; }
}
