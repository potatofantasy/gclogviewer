/*
 * Copyright 2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */
package code.google.gclogviewer.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import com.sun.swing.internal.plaf.basic.resources.basic;

public class GCParserDriver
{
    private final GCStats gcStats;
    private final int _next_arg;

    private BitSet _actions;
    private String _prefix;
    private String _suffix;
    private int _cpu_count;
    private boolean _has_time_zero;

    private static List<GCParser> gcParserList;
    private List<GCParser> usedGCParserList = new ArrayList<GCParser>();

    private List<GCParser> createGCParsers(GCStats gcStats, boolean verbose)
    {
	List<GCParser> gcParserList = new ArrayList<GCParser>();
	gcParserList.add(new StopAppTimeGCParser(gcStats, verbose));
	gcParserList.add(new ParGCYoungGCParser(gcStats, verbose));
	gcParserList.add(new FWYoungGCParser(gcStats, verbose));
	gcParserList.add(new ParGCFullGCParser(gcStats, verbose));
	gcParserList.add(new CMSGCParser(gcStats, verbose));
	gcParserList.add(new ParCompactPhaseGCParser(gcStats, verbose));
	gcParserList.add(new FWOldGCParser(gcStats, verbose));
	gcParserList.add(new FWFullGCParser(gcStats, verbose));
	gcParserList.add(new VerboseGCParser(gcStats, verbose));

	return gcParserList;

    }

    public GCParserDriver(BitSet actions, EnumMap<GCMetric, Boolean> enabled_map, String prefix, String suffix, int cpu_count)
    {
	_prefix = prefix;
	_suffix = suffix;
	_cpu_count = cpu_count;
	_next_arg = 0;
	_has_time_zero = true;

	gcStats = create_gc_stats(_actions, null, _cpu_count, _has_time_zero);
	gcParserList = createGCParsers(gcStats, true);
    }

    public GCParserDriver(BitSet actions)
    {
	this(actions, null, null, ".dat", 1);
    }

    public GCStats gc_stats()
    {
	return gcStats;
    }

    public BitSet actions()
    {
	return _actions;
    }

    public String prefix()
    {
	return _prefix;
    }

    public String suffix()
    {
	return _suffix;
    }

    public boolean parse(List<GCParser> parsers, String filename, int line, String s)
    {
	Iterator<GCParser> iterator = parsers.iterator();
	boolean matched = false;
	do
	{
	    matched = iterator.next().parse(filename, line, s);
	} while (!matched && iterator.hasNext());
	return matched;
    }

    public void parse(BufferedReader r, String filename) throws IOException
    {
	int line = 0;
	long matches = 0;
	String s = r.readLine();
	while (s != null)
	{
	    ++line;
	    boolean usedBefore = false;
	    for (GCParser gcParser : usedGCParserList)
	    {

		if (gcParser.parse(filename, line, s))
		{
		    matches++;
		    usedBefore = true;
		    break;
		}
	    }

	    if (!usedBefore)
	    {
		for (GCParser gcParser : gcParserList)
		{
		    if (gcParser.parse(filename, line, s))
		    {
			matches++;
			usedGCParserList.add(gcParser);
			break;
		    }
		}
	    }

	    s = r.readLine();
	}
	
	    if(isUsedParser(CMSGCParser.class))
	    {
		gcStats.setGcType(GCType.CONC_MARK_SWEEP_GC);
	    }
	    else if(isUsedParser(FWYoungGCParser.class)&&isUsedParser(FWOldGCParser.class))
	    {
		gcStats.setGcType(GCType.SERIAL_GC);
		
	    }else if(isUsedParser(ParGCYoungGCParser.class)&&isUsedParser(ParGCYoungGCParser.class))
	    {
		gcStats.setGcType(GCType.PARALLEL_GC);
	    }
	
	gcStats.end_of_file();
    }

    public void parse(InputStream is, String filename) throws IOException
    {
	InputStreamReader ir = new InputStreamReader(is);
	parse(new BufferedReader(ir), filename);
    }

    public void parse(File file) throws IOException
    {
	FileReader fr = new FileReader(file);
	parse(new BufferedReader(fr), file.getName());
	fr.close();
    }

    public static void compare_statistics(PrintStream s, String ref_name, GCStats ref_stats, String new_name, GCStats new_stats, boolean terse)
    {
	s.println(ref_name + " vs. " + new_name);
	ref_stats.print_comparison(s, new_stats, terse);
    }

    public static void compare_statistics(PrintStream s, String ref_name, GCParserDriver ref_driver, String new_name, GCParserDriver new_driver, boolean terse)
    {
	compare_statistics(s, ref_name, ref_driver.gc_stats(), new_name, new_driver.gc_stats(), terse);
    }

    public void print_statistics(PrintStream s)
    {
	gcStats.print(s);
    }

    public void print_statistics(PrintStream s, String name)
    {
	s.println(name);
	print_statistics(s);
    }

    public void save_data(String prefix, String suffix) throws IOException
    {
	gcStats.save(prefix, suffix);
    }

    public void save_data() throws IOException
    {
	save_data(_prefix, _suffix);
    }

    public void describe_metrics(PrintStream s)
    {
	ResourceBundle b = ResourceBundle.getBundle("GCMetricHelp");

	if (b.containsKey("intro"))
	    s.println(b.getString("intro"));
	for (GCMetric metric : GCMetric.values())
	{
	    String name = metric.name();
	    s.println(name + '\t' + b.getString(name));
	}
	if (b.containsKey("closing"))
	    s.println(b.getString("closing"));
    }

    public void parse_output_file_pattern(String s)
    {
	final int pattern_length = 9;
	int pos = s.indexOf("%{metric}");
	if (pos < 0)
	{
	    _prefix = s;
	    _suffix = "";
	    return;
	}

	_prefix = s.substring(0, pos);
	_suffix = s.substring(pos + pattern_length);
    }

    protected int next_arg()
    {
	return _next_arg;
    }

    public static GCMetric[] parse_metric_names(String names[], Collection<String> unrecognized)
    {
	if (names == null || names.length == 0)
	    return null;
	int errors = 0;

	ArrayList<GCMetric> metrics;
	metrics = new ArrayList<GCMetric>(names.length);
	for (int i = 0; i < names.length; ++i)
	{
	    GCMetric metric = GCMetric.metric(names[i]);
	    if (metric != null)
	    {
		metrics.add(metric);
	    } else if (unrecognized != null)
	    {
		// Unrecognized name.
		unrecognized.add(names[i]);
	    }
	}

	if (metrics.size() == 0)
	    return null;
	return metrics.toArray(new GCMetric[metrics.size()]);
    }

    public static GCMetric[] parse_metric_names(String names[])
    {
	return parse_metric_names(names, null);
    }

    public static GCMetric[] parse_metric_names(String list, Collection<String> unrecognized)
    {
	if (list == null)
	    return null;
	return parse_metric_names(list.split("[ \t,:]+"), unrecognized);
    }

    public static GCMetric[] parse_metric_names(String list)
    {
	return parse_metric_names(list, null);
    }

    /**
     * Creates and returns a map that indicates whether each GCMetric is enabled
     * or disabled.
     * 
     * <p>
     * The enabled status of the remaining metrics (those <b>not</b> listed in
     * metrics[]) is set to !value.
     * </p>
     * 
     * @param metrics
     *            [] an array of metrics, or null.
     * 
     * @param value
     *            the enabled status stored for each metric in metrics[].
     * 
     * @returns a map giving the enabled status of each GCMetric, or null.
     */
    public static EnumMap<GCMetric, Boolean> create_enabled_map(GCMetric metrics[], boolean value)
    {
	if (metrics == null)
	    return null;

	EnumMap<GCMetric, Boolean> map;
	map = new EnumMap<GCMetric, Boolean>(GCMetric.class);
	for (GCMetric metric : GCMetric.values())
	{
	    map.put(metric, !value);
	}

	for (int i = 0; i < metrics.length; ++i)
	{
	    map.put(metrics[i], value);
	}

	return map;
    }

    public static EnumMap<GCMetric, Boolean> create_enabled_map(String metrics, boolean value, Collection<String> unrecognized)
    {
	if (metrics == null)
	    return null;
	GCMetric m[] = parse_metric_names(metrics, unrecognized);
	return create_enabled_map(m, value);
    }

    protected GCStats create_gc_stats(BitSet actions, EnumMap<GCMetric, Boolean> enabled_map, int cpu_count, boolean input_has_time_zero)
    {
	return new GCDataStore(enabled_map, cpu_count, input_has_time_zero);
    }

    /**
     * Sort the GCParsers in descending order by match_count.
     */
    protected ArrayList<GCParser> sort_gc_parsers(ArrayList<GCParser> parsers)
    {
	final int n = parsers.size();

	// Insertion sort.
	for (int i = 1; i < n; ++i)
	{
	    GCParser parser_i = parsers.get(i);
	    long value = parser_i.match_count();
	    int j = i - 1;
	    while (j >= 0 && parsers.get(j).match_count() < value)
	    {
		parsers.set(j + 1, parsers.get(j));
		--j;
	    }
	    parsers.set(j + 1, parser_i);
	}

	return parsers;
    }
    
    protected boolean isUsedParser(Class<? extends GCParser> clazz)
    {
	for(GCParser gcParser:usedGCParserList)
	{
	    if(gcParser.getClass().equals(clazz))
	    {
		return true;
	    }
	}
	
	return false;
    }

}
