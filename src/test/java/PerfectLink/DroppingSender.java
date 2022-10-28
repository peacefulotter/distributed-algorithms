package PerfectLink;

import cs451.parser.ParserResult;
import cs451.perfectlink.Sender;

public class DroppingSender extends Sender
{
    private static final double DROPPING_RATE = 0.25d;

    public DroppingSender( ParserResult result )
    {
        super( result.host, result.dest, result.output, result.config );
    }

    /*@Override
    protected boolean broadcastAndAck()
    {
        if ( Math.random() <= DROPPING_RATE )
        {
            log( "Voluntarily dropping packet" );
            return false;
        }
        return super.broadcastAndAck();
    }*/
}
