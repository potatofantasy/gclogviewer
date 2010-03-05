package code.google.gclogviewer.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StopAppTimeGCParser extends GCParser
{

    private static final String prefix = "Total time for which application threads were stopped:";
    private static final int STOP_TIME = 1;

    private static Pattern pattern = Pattern.compile(prefix + " ([0-9]+\\.[0-9]+) seconds");

    public StopAppTimeGCParser(GCStats gcstats, boolean verbose)
    {
	super(gcstats, verbose);
    }

    @Override
    public boolean parse(String filename, int line, String s)
    {
	Matcher matcher = pattern.matcher(s);
	if (matcher.find())
	{
	    addStopAppTime(matcher, STOP_TIME);
	}

	return false;
    }

}
