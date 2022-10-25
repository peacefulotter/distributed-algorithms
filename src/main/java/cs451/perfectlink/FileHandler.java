package cs451.perfectlink;

import cs451.packet.Packet;

import java.io.*;
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
        messages.add( p.getFileLine() );
    }

    /**
     * Writes the 'messages' list to the output file and clear the said list
     */
    public void write()
    {
        System.out.println("WRITING TO FILE");
        System.out.println(messages);
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
