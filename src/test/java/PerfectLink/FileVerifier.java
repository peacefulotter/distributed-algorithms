package PerfectLink;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

class FileVerifier
{
    public static boolean verifySender(int id, int nbSenders, int nbMessages)
    {
        String path = "../example/output/" + id + ".output";
        Set<String> verifier = new HashSet<>();

        try ( BufferedReader reader = new BufferedReader( new FileReader( path ) ) )
        {
            String line;
            while ( (line = reader.readLine()) != null )
            {
                boolean added = verifier.add( line );
                if ( !added ) {
                    System.out.println(id + " contains duplicates");
                    return false;
                }
            }
        } catch ( IOException e )
        {
            e.printStackTrace();
        }

        return verifier.size() == nbSenders * nbMessages;
    }
}
