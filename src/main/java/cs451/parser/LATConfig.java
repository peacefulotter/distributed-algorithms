package cs451.parser;

import java.io.*;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LATConfig
{
    public final int p, vs, ds;
    public final Queue<Set<Integer>> proposals;

    public LATConfig( String path )
    {
        try ( BufferedReader r = new BufferedReader( new FileReader( path ) ) )
        {
            String line = r.readLine();

            String[] split = line.split( " " );
            p = Integer.parseInt( split[0] );
            vs = Integer.parseInt( split[1] );
            ds = Integer.parseInt( split[2] );

            proposals = new ArrayDeque<>(p);

            while ( (line = r.readLine()) != null)
            {
                proposals.add(
                    Arrays.stream( line.split( " " ) )
                        .map( Integer::parseInt )
                        .collect( Collectors.toSet() )
                );
            }

            System.out.println(p);
            System.out.println(vs);
            System.out.println(ds);
            System.out.println(proposals);

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

    public Queue<Set<Integer>> getProposals()
    {
        return proposals;
    }
}
