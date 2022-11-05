package cs451.parser;

import cs451.utils.Constants;

public class Parser {

    private String[] args;
    private long pid;
    private IdParser idParser;
    private HostsParser hostsParser;
    private OutputParser outputParser;
    private ConfigParser configParser;

    public Parser(String[] args) {
        this.args = args;
    }

    public HostsParser parse()
    {
        pid = ProcessHandle.current().pid();

        idParser = new IdParser();
        hostsParser = new HostsParser();
        outputParser = new OutputParser();
        configParser = new ConfigParser();

        int argsNum = args.length;
        if ( argsNum != Constants.ARG_LIMIT_CONFIG )
        {
            help( "argsNum" );
        }

        if ( !idParser.populate( args[Constants.ID_KEY], args[Constants.ID_VALUE] ) )
        {
            help( "idParser" );
        }

        if ( !hostsParser.populate( args[Constants.HOSTS_KEY], args[Constants.HOSTS_VALUE] ) )
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

        return hostsParser;
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
