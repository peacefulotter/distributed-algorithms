package PerfectLink;


import org.junit.jupiter.api.Test;

public class IndexTest
{
    @Test
    public void index()
    {
        for ( int i = 0; i < 100; i++ )
        {
            System.out.println(i / 8 + " " + i % 8);
        }
    }

}
