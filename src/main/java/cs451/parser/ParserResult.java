package cs451.parser;

import cs451.Host;

import java.util.List;

public class ParserResult
{
    public final Host host;
    public final List<Host> hosts;
    public final String output;
    public final FIFOConfig config;

    public ParserResult( Host host, List<Host> hosts, String output, FIFOConfig config )
    {
        this.host = host;
        this.hosts = hosts;
        this.output = output;
        this.config = config;
    }

    @Override
    public String toString()
    {
        return "ParserResult{" +
            "host=" + host +
            ", hosts='" + hosts + '\'' +
            ", output='" + output + '\'' +
            ", config=" + config +
            '}';
    }
}
