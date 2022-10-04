package cs451.parser.perfectlink;

import java.io.*;

public class FileHandler
{
    private final String path;

    public FileHandler( String path )
    {
        this.path = path;
    }
    private InputStream resourceStream( String resourceName )
    {
        return getClass().getResourceAsStream( resourceName );
    }

    public void write( String m )
    {
        try (PrintWriter pw = new PrintWriter( path + "_car.csv"))
        {
            pw.println();
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
