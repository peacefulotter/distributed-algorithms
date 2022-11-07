package cs451.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FIFOConfig
{
    private final int m;

    public FIFOConfig( String path )
    {
        try ( InputStream stream = new FileInputStream( path ) )
        {
            String content = new String( stream.readAllBytes(), StandardCharsets.UTF_8 );
            m = Integer.parseInt( content.trim() );
        } catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public int getM() { return m; }
}
