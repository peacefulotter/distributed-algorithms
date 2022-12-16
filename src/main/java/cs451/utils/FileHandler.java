package cs451.utils;

import cs451.lat.Proposal;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class FileHandler
{
    private final String path;
    private final List<Set<Integer>> messages;

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

    public void register( int round, Proposal proposal )
    {
        Logger.log("[FileHandler] Registering " + round + " pro: " + proposal + " size: " + messages.size());
        int s = messages.size();
        if ( round == s )
            messages.add( proposal );
        else if ( round < s )
            messages.set( round, proposal );
        else
        {
            for ( int i = 0; i < round - s; i++ )
                messages.add( null );
            messages.add( proposal );
        }
    }

    /**
     * Writes the 'messages' list to the output file and clear the said list
     */
    public void write()
    {
        try ( PrintWriter pw = new PrintWriter( new FileOutputStream( path, true ) ) )
        {
            System.out.println("Writing messages " + messages);
            for ( Set<Integer> line : messages )
            {
                StringJoiner sj = new StringJoiner( " " );
                line.forEach( i -> sj.add( String.valueOf( i ) ) );
                pw.println( sj );
            }
        } catch ( IOException e )
        {
            e.printStackTrace();
        }
        messages.clear();
    }
}
