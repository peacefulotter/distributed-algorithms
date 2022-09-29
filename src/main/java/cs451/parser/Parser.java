package cs451.parser;

import cs451.Constants;
import cs451.Host;
import cs451.parser.perfectlink.PLConfigParser;
import cs451.parser.perfectlink.PLHost;

import java.util.List;

public class Parser {

    private String[] args;
    private long pid;
    private IdParser idParser;
    private HostsParser<PLHost> hostsParser;
    private OutputParser outputParser;
    private PLConfigParser configParser;

    public Parser(String[] args) {
        this.args = args;
    }

    public List<PLHost> parse()
    {
        pid = ProcessHandle.current().pid();

        idParser = new IdParser();
        hostsParser = new HostsParser<>();
        outputParser = new OutputParser();
        configParser = new PLConfigParser();

        int argsNum = args.length;
        if ( argsNum != Constants.ARG_LIMIT_CONFIG )
        {
            help( "argsNum" );
        }

        if ( !idParser.populate( args[Constants.ID_KEY], args[Constants.ID_VALUE] ) )
        {
            help( "idParser" );
        }

        if ( !hostsParser.populate( PLHost.class, args[Constants.HOSTS_KEY], args[Constants.HOSTS_VALUE] ) )
        {
            help( "hostsParser - populate" );
        }

        if ( !hostsParser.inRange( idParser.getId() ) )
        {
            help( "hostsParser - inRange" );
        }

        if ( !outputParser.populate( args[Constants.OUTPUT_KEY], args[Constants.OUTPUT_VALUE] ) )
        {
            help( "outputParser - populate" );
        }

        if ( !configParser.populate( args[Constants.CONFIG_VALUE] ) )
        {
            help( "configParser - populate" );
        }

        configParser.read();
        System.out.println("[CONFIG] m: " + configParser.getM() + ", i: " + configParser.getI());
        List<PLHost> hosts = hostsParser.getHosts();
        hosts.forEach( h -> h.setParams( configParser.getM(), configParser.getI() ));
        return hosts;
    }

    private void help(String msg) {
        System.err.println("Usage: ./run.sh --id ID --hosts HOSTS --output OUTPUT CONFIG");
        System.err.println(msg);
        System.exit(1);
    }

    public int myId() {
        return idParser.getId();
    }

    public String output() {
        return outputParser.getPath();
    }

    public String config() {
        return configParser.getPath();
    }

}
