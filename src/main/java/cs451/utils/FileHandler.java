package cs451.utils;

import cs451.packet.Packet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileHandler
{
    private final String path;
    private final ConcurrentLinkedQueue<String> messages;

    public FileHandler( String path )
    {
        this.path = path;
        this.messages = new ConcurrentLinkedQueue<>();
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
        messages.addAll( p.getFileLines() );
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
