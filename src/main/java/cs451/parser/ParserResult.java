package cs451.parser;

import cs451.Host;
import cs451.perfectlink.PLConfig;

public class ParserResult
{
    public final Host host, dest;
    public final String output;
    public final PLConfig config;

    public ParserResult( Host host, Host dest, String output, PLConfig config )
    {
        this.host = host;
        this.dest = dest;
        this.output = output;
        this.config = config;
    }
}
