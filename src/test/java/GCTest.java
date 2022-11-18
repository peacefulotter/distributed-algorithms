import cs451.utils.WithGC;
import org.junit.jupiter.api.Test;

public class GCTest
{
    @Test
    public void test()
    {
        int maxSize = 15;
        WithGC<Integer> gc = new WithGC<>( maxSize );
        for ( int i = 0; i < 1000; i++ )
        {
            gc.add( i );
            for ( int j = i; j > i - maxSize - 2 && j >= 0; j-- )
            {
                System.out.println(j + " " + gc.contains( j ) + " " + gc );
            }
        }
    }
}
