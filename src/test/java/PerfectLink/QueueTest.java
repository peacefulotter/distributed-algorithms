package PerfectLink;

import cs451.network.SeqMsg;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;

public class QueueTest
{
    private static final int max = 8;

    private Queue<SeqMsg> getQueue( int nbMessages )
    {
        Queue<SeqMsg> queue = new ArrayDeque<>();
        int seqNr;
        for ( seqNr = 1; seqNr < nbMessages + 1; seqNr += max )
        {
            // try to send a maximum of 8 messages per packet
            int messages = Math.min(max, nbMessages - seqNr + 1);
            queue.add( new SeqMsg( seqNr, messages ) );
        }
        return queue;
    }

    public SeqMsg getNext( int seqNr, int nbMessages )
    {
        int nextSeq = seqNr + max;
        int nextMsg = Math.min( max, nbMessages - nextSeq + 1);
        return new SeqMsg( nextSeq, nextMsg );
    }

    @Test
    public void testNext()
    {
        for ( int i = 8; i < 1000; i++ )
        {
            Queue<SeqMsg> q = getQueue( i );
            System.out.println(i + " " + q.size() + " " + (i / max + ((i % max > 0) ? 1 : 0) ));
            int seq;
            SeqMsg prev = new SeqMsg( 1, 8 );
            while ( !q.isEmpty() )
            {
                SeqMsg sm = q.poll();
                if ( !sm.equals( prev ) )
                    throw new RuntimeException("dif");
                seq = sm.seqNr;
                prev = getNext( seq, i );
            }
        }
    }
}
