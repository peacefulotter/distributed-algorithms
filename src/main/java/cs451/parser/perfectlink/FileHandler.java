package cs451.parser.perfectlink;

import cs451.parser.packet.Packet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler
{
    private final String path;
    private final List<String> messages;

    public FileHandler( String path )
    {
        this.path = path;
        this.messages = new ArrayList<>();
        onInit();
    }

    /**
     * Clear file content
     */
    private void onInit()
    {
        try ( PrintWriter writer = new PrintWriter(path) )
        {
            writer.print("");
        } catch ( FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }

    public void register( Packet p )
    {
        // TODO: improve this?
        messages.add( p.getMsg() );
    }

    /**
     * Writes the 'messages' list to the output file and clear the said list
     */
    public void write()
    {
        try ( PrintWriter pw = new PrintWriter( new FileOutputStream( path, true ) ) )
        {
            messages.forEach( pw::println );
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
        messages.clear();
    }
}
