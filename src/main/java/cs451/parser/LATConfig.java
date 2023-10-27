package cs451.parser;

import cs451.lat.Proposal;
import cs451.packet.PacketContent;
import cs451.packet.PacketTypes;

import java.io.*;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LATConfig
{
    public final int p, vs, ds;
    public final Queue<PacketContent> contentsQueue;

    public LATConfig( String path )
    {
        try ( BufferedReader r = new BufferedReader( new FileReader( path ) ) )
        {
            String line = r.readLine();

            String[] split = line.split( " " );
            p = Integer.parseInt( split[0] );
            vs = Integer.parseInt( split[1] );
            ds = Integer.parseInt( split[2] );

            contentsQueue = new ArrayDeque<>(p);
            int round = 0;

            while ( (line = r.readLine()) != null)
            {
                Proposal prop = new Proposal();
                String[] props = line.split( " " );
                for ( String s : props )
                    prop.add( Integer.parseInt( s ) );
                PacketContent content = new PacketContent( PacketTypes.LAT_PROP, round++,0, prop );
                contentsQueue.add( content );
            }
        } catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public int getP()
    {
        return p;
    }

    public int getVs()
    {
        return vs;
    }

    public int getDs()
    {
        return ds;
    }

    public Queue<PacketContent> getContentsQueue()
    {
        return contentsQueue;
    }
}
