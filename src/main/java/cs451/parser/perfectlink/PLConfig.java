package cs451.parser.perfectlink;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PLConfig
{
    private final int m, i;

    public PLConfig( String path )
    {
        try ( InputStream stream = new FileInputStream( path ) )
        {
            String content = new String( stream.readAllBytes(), StandardCharsets.UTF_8 );
            String[] split = content.split( " " );
            m = Integer.parseInt( split[0].trim() );
            i = Integer.parseInt( split[1].trim() );
            System.out.println("PLConfigParser - m: " + m + ", i: " + i);
        } catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public int getM() { return m; }
    public int getI() { return i; }
}
