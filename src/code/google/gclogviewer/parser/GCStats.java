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

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GCStats
{
    public static final String eol = System.getProperty("line.separator");
    public static final String hdr1 = "     what      count     total        min         max         mean      stddev";
    public static final String hdr2 = "------------- ------ ------------ ----------- ----------- ----------- ---------";
    public static final String hdr = hdr1 + eol + hdr2;

    public static final String data_fmt_str = "%-13s %6d %12.3f %11.4f %11.4f %11.4f %9.4f";
    public static final String data_pct_chg_fmt_str = "%-13s %6.2f%% %11.3f%% %10.4f%% %10.4f%% %10.4f%% %8.4f%%";
    public static final String rate_fmt_str = "%-18s = %12.3f / %12.3f = %9.3f %s/%s";
    public static final String load_fmt_str = "%-18s = %12.3f / %12.3f = %9.3f%%";
    // -----------------------------------------------------------------------------------------------------------------
    private DecimalFormat doubleformat = new DecimalFormat("#0.0000");

    private EnumMap<GCMetric, Stats> statsMap;
    private EnumMap<GCMetric, Boolean> enabledMap;

    // key: time value: consumetime
    private Map<Double, Double> minorGCConsumeTimes = new HashMap<Double, Double>();
    // key: time value: String[0]: memoryBefore String[1]: memoryAfter
    private Map<Double, Double[]> minorGCMemoryChanges = new HashMap<Double, Double[]>();
    // key: time value: consumetime
    private Map<Double, Double> fullGCConsumeTimes = new HashMap<Double, Double>();
    // key: time value: String[0]: memoryBefore String[1]: memoryAfter
    private Map<Double, Double[]> fullGCMemoryChanges = new HashMap<Double, Double[]>();
    // key: time value: consumetime
    private Map<Double, Double> cmsGCConsumeTimes = new HashMap<Double, Double>();
    // key: time value: Double[0]: memoryBefore Double[1]: memoryAfter
    private Map<Double, Double[]> cmsGCMemoryChanges = new HashMap<Double, Double[]>();

    private List<Double> stopTimeList = new ArrayList<Double>();

    private double sumAppStoppedTime = 0;
    private double maxAppStoppedTime = 0;
    private int cmsGCPromotionFailedCount = 0;
    private int cmsGCConcurrentModeFailedCount = 0;
    
    private double gcLastLogTime = 0;
    private double ygUsedBeg;
    private double ygUsedEnd;
    private double ygCommitEnd;
    private double ygTimestampBeg;
    private double ygTimestampEnd;
    private double thUsedBeg;
    private double thUsedEnd;
    private double thCommitEnd;
    private double thTimestampBeg;
    private double thTimestampEnd;
    private double timestampBeg;
    private double timestampEnd;
    private double TimestampOfs;
    private double elapsedTime;
    private GCType gcType;
    

    // Counter for generating timestamps when they are not present in the
    // input.
    private long _timestamp;

    private TimingWindowData _window_data;

    private final int _cpu_count;

    // Desired "gap" (in seconds) between timestamps from one file to the
    // next. The first timestamp in the second and subsequent files is
    // rounded up to a multiple of this number.
    private final int _file_timestamp_gap;
    // Minimum "gap" between timestamps from one file to the next.
    private final int _file_timestamp_gap_min;

    // Whether every input stream contains all data from VM start ("time
    // zero") through the end of the stream.
    private final boolean _input_has_time_zero;

    GCStats(EnumMap<GCMetric, Boolean> enabled_map, int cpu_count, boolean input_has_time_zero, int file_timestamp_gap, int file_timestamp_gap_min)
    {
	enabledMap = enabled_map;
	statsMap = new EnumMap<GCMetric, Stats>(GCMetric.class);
	for (GCMetric metric : GCMetric.values())
	{
	    statsMap.put(metric, new Stats());
	}
	_cpu_count = cpu_count;
	_window_data = new TimingWindowData(cpu_count);

	_input_has_time_zero = input_has_time_zero;
	if (!_input_has_time_zero)
	    timestampBeg = -1.0;

	_file_timestamp_gap = file_timestamp_gap;
	_file_timestamp_gap_min = file_timestamp_gap_min;

	thTimestampBeg = thTimestampEnd = timestampBeg;
    }

    GCStats(EnumMap<GCMetric, Boolean> enabled_map, int cpu_count, boolean input_has_time_zero)
    {
	this(enabled_map, cpu_count, input_has_time_zero, 600, 300);
    }

    public void add(GCMetric metric, double val)
    {
	statsMap.get(metric).add(val);
    }

    public void add(GCMetric metric, String s)
    {
	statsMap.get(metric).add(s);
    }

    public void add_timestamp(GCMetric metric, double beg, double end)
    {
	if (timestampBeg < 0.0)
	{
	    // The first timestamp in a new input stream. Should
	    // only occur when _input_has_time_zero is false.
	    timestampBeg = TimestampOfs + beg;
	}
	timestampEnd = TimestampOfs + end;
    }

    // Generate a timestamp. Not thread safe.
    public double generate_timestamp()
    {
	final double ts = _timestamp;
	++_timestamp;
	return ts;
    }

    public boolean has_generated_timestamps()
    {
	return _timestamp != 0;
    }

    public double timestamp_offset()
    {
	return TimestampOfs;
    }

    private void eof_debug(boolean before)
    {
	PrintStream s = System.err;
	if (before)
	{
	    s.println("before:  tsaz=" + _input_has_time_zero + " gap=" + _file_timestamp_gap + " min_gap=" + _file_timestamp_gap_min);
	    s.println("ofs=" + TimestampOfs + " elapsed=" + elapsedTime);
	    s.println("tb=" + timestampBeg + " te=" + timestampEnd);
	    s.println("delta=" + (timestampEnd - timestampBeg));
	} else
	{
	    s.println("after:  ");
	    s.println("ofs=" + TimestampOfs + " elapsed=" + elapsedTime);
	    s.println("tb=" + timestampBeg + " te=" + timestampEnd);
	}
    }

    /**
     * This must be called after each file or input stream has been read to
     * maintain accurate timestamps.
     */
    public void end_of_file()
    {
	// eof_debug(true);

	elapsedTime += timestampEnd - timestampBeg;

	long end = (long) Math.ceil(timestampEnd);
	long addend = _file_timestamp_gap + _file_timestamp_gap_min - 1;
	TimestampOfs = (end + addend) / _file_timestamp_gap * _file_timestamp_gap;

	timestampBeg = _input_has_time_zero ? TimestampOfs : -1.0;
	timestampEnd = timestampBeg;

	// eof_debug(false);
    }

    public int cpu_count()
    {
	return _cpu_count;
    }

    public Stats stats(GCMetric metric)
    {
	return statsMap.get(metric);
    }

    // The sizes of the young gen are saved after each GC (young or full)
    // and may be used to compute the amount allocated:
    // 
    // allocated = gc[n].amount_used_at_beg - gc[n-1].amount_used_at_end
    public void save_yg_info(double used_beg, double used_end, double commit_end, double timestamp_beg, double timestamp_end)
    {
	ygUsedBeg = used_beg;
	ygUsedEnd = used_end;
	ygCommitEnd = commit_end;
	ygTimestampBeg = timestamp_beg;
	ygTimestampEnd = timestamp_end;
    }

    public TimingWindowData timing_window()
    {
	return _window_data;
    }

    // The sizes of the total heap are saved after each GC (young or full)
    // and used to compute the amount allocated:
    // 
    // allocated = gc[n].amount_used_at_beg - gc[n-1].amount_used_at_end
    public void save_heap_sizes(double th_used_beg, double th_used_end, double th_commit_end, double timestamp_beg, double timestamp_end)
    {
	thUsedBeg = th_used_beg;
	thUsedEnd = th_used_end;
	thCommitEnd = th_commit_end;
	thTimestampEnd = timestamp_beg;
	thTimestampEnd = timestamp_end;
    }

    public double heap_used_beg()
    {
	return thUsedBeg;
    }

    public double heap_used_end()
    {
	return thUsedEnd;
    }

    public double heap_commit_end()
    {
	return thCommitEnd;
    }

    public double yg_used_beg()
    {
	return ygUsedBeg;
    }

    public double yg_used_end()
    {
	return ygUsedEnd;
    }

    public double yg_commit_end()
    {
	return ygCommitEnd;
    }

    public double heap_timestamp_beg()
    {
	return thTimestampBeg;
    }

    public double heap_timestamp_end()
    {
	return thTimestampEnd;
    }

    public double yg_timestamp_beg()
    {
	return ygTimestampBeg;
    }

    public double yg_timestamp_end()
    {
	return ygTimestampEnd;
    }

    public double elapsed_time()
    {
	return elapsedTime;
    }

    public static String format(String name, Stats stats)
    {
	final long n = stats.n();
	if (n > 0)
	{
	    return String.format(data_fmt_str, name, n, stats.sum(), stats.min(), stats.max(), stats.mean(), n > 1 ? stats.stddev() : 0.0);
	}

	return String.format(data_fmt_str, name, 0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    public static double percent_change(double ref_val, double new_val)
    {
	if (ref_val != 0.0)
	{
	    return (new_val - ref_val) / ref_val * 100.0;
	}
	return 0.0;
    }

    public static String format_percent_change(String name, Stats ref_stats, Stats new_stats)
    {
	final long ref_n = ref_stats.n();
	final long new_n = new_stats.n();

	final double mean_pct_chg = ref_n > 0 && new_n > 0 ? percent_change(ref_stats.mean(), new_stats.mean()) : 0.0;
	final double stddev_pct_chg = ref_n > 1 && new_n > 1 ? percent_change(ref_stats.stddev(), new_stats.stddev()) : 0.0;

	return String.format(data_pct_chg_fmt_str, name, percent_change(ref_n, new_n), percent_change(ref_stats.sum(), new_stats.sum()), percent_change(ref_stats.min(), new_stats.min()), percent_change(ref_stats.max(), new_stats.max()), mean_pct_chg, stddev_pct_chg);
    }

    public static String format_rate(String name, double total, String total_units, double period, String period_units)
    {
	return String.format(rate_fmt_str, name, total, period, period != 0.0 ? total / period : 0.0, total_units, period_units);
    }

    public static String format_rate(String name, double total, String total_units, double period, String period_units, double pct_chg)
    {
	String s = format_rate(name, total, total_units, period, period_units);
	StringBuilder b = new StringBuilder(s);
	b.append(String.format(" %7.3f%%", pct_chg));
	return b.toString();
    }

    public static String format_load(String name, double total, double period)
    {
	return String.format(load_fmt_str, name, total, period, period != 0.0 ? total / period * 100.0 : 0.0);
    }

    public static String format_load(String name, double total, double period, double pct_chg)
    {
	String s = format_load(name, total, period);
	StringBuilder b = new StringBuilder(s);
	b.append(String.format(" %7.3f%%", pct_chg));
	return b.toString();
    }

    public static void print(PrintStream s, String name, Stats stats)
    {
	s.println(format(name, stats));
    }

    public boolean enabled(GCMetric metric)
    {
	return enabledMap == null || enabledMap.get(metric);
    }

    public boolean disabled(GCMetric metric)
    {
	return !enabled(metric);
    }

    public void print_stats(PrintStream s)
    {
	for (GCMetric metric : GCMetric.values())
	{
	    Stats m_stats = stats(metric);
	    if (enabled(metric) && m_stats.n() > 0)
	    {
		print(s, metric.name(), m_stats);
	    }
	}
    }

    public class RateVars
    {
	public RateVars(GCStats gcstats)
	{
	    alloc = gcstats.stats(GCMetric.th_alloc).sum();
	    promo = gcstats.stats(GCMetric.yg_promo).sum();
	    elapsed_time = gcstats.elapsed_time();
	    ygc_time = gcstats.stats(GCMetric.ygc_time).sum();
	    tgc_time = gcstats.stats(GCMetric.tgc_time).sum();
	    cgc_time = gcstats.stats(GCMetric.cms_cm_a_time).sum() + gcstats.stats(GCMetric.cms_cp_a_time).sum() + gcstats.stats(GCMetric.cms_cs_a_time).sum() + gcstats.stats(GCMetric.cms_cr_a_time).sum();
	    cpu_time_tot = elapsed_time * gcstats.cpu_count();
	    cpu_time_stw = tgc_time * gcstats.cpu_count();
	    // XXX - need an estimate of the average number of
	    // threads used during concurrent phases.
	    cpu_time_cgc = cgc_time;
	    cpu_time_mut = cpu_time_tot - cpu_time_stw - cpu_time_cgc;
	}

	public final double alloc;
	public final double promo;
	public final double elapsed_time;
	public final double ygc_time;
	public final double tgc_time;
	public final double cgc_time;
	public final double cpu_time_tot;
	public final double cpu_time_stw;
	public final double cpu_time_cgc;
	public final double cpu_time_mut;
    };

    public void print_rates(PrintStream s, RateVars x)
    {
	String MiB = "MiB";
	s.println(format_rate("alloc/elapsed_time", x.alloc, MiB, x.elapsed_time, "s"));
	s.println(format_rate("alloc/tot_cpu_time", x.alloc, MiB, x.cpu_time_tot, "s"));
	s.println(format_rate("alloc/mut_cpu_time", x.alloc, MiB, x.cpu_time_mut, "s"));
	s.println(format_rate("promo/elapsed_time", x.promo, MiB, x.elapsed_time, "s"));
	s.println(format_rate("promo/ygc_time", x.promo, MiB, x.ygc_time, "s"));

	s.println(format_load("gc_stw_load", x.cpu_time_stw, x.cpu_time_tot));
	s.println(format_load("gc_concurrent_load", x.cpu_time_cgc, x.cpu_time_tot));
	s.println(format_load("gc_load", x.cpu_time_stw + x.cpu_time_cgc, x.cpu_time_tot));
    }

    public static void print_comparison(PrintStream s, RateVars x, RateVars y)
    {
	double rate_x;
	double rate_y;

	String MiB = "MiB";

	rate_x = x.alloc / x.elapsed_time;
	rate_y = y.alloc / y.elapsed_time;
	s.println(format_rate("alloc/elapsed_time", x.alloc, MiB, x.elapsed_time, "s"));
	s.println(format_rate("alloc/elapsed_time", y.alloc, MiB, y.elapsed_time, "s", percent_change(rate_x, rate_y)));

	rate_x = x.alloc / x.cpu_time_tot;
	rate_y = y.alloc / y.cpu_time_tot;
	s.println(format_rate("alloc/tot_cpu_time", x.alloc, MiB, x.cpu_time_tot, "s"));
	s.println(format_rate("alloc/tot_cpu_time", y.alloc, MiB, y.cpu_time_tot, "s", percent_change(rate_x, rate_y)));

	rate_x = x.alloc / x.cpu_time_mut;
	rate_y = y.alloc / y.cpu_time_mut;
	s.println(format_rate("alloc/mut_cpu_time", x.alloc, MiB, x.cpu_time_mut, "s"));
	s.println(format_rate("alloc/mut_cpu_time", y.alloc, MiB, y.cpu_time_mut, "s", percent_change(rate_x, rate_y)));

	rate_x = x.promo / x.elapsed_time;
	rate_y = y.promo / y.elapsed_time;
	s.println(format_rate("promo/elapsed_time", x.promo, MiB, x.elapsed_time, "s"));
	s.println(format_rate("promo/elapsed_time", y.promo, MiB, y.elapsed_time, "s", percent_change(rate_x, rate_y)));

	rate_x = x.promo / x.ygc_time;
	rate_y = y.promo / y.ygc_time;
	s.println(format_rate("promo/ygc_time", x.promo, MiB, x.ygc_time, "s"));
	s.println(format_rate("promo/ygc_time", y.promo, MiB, y.ygc_time, "s", percent_change(rate_x, rate_y)));

	rate_x = x.cpu_time_stw / x.cpu_time_tot;
	rate_y = y.cpu_time_stw / y.cpu_time_tot;
	s.println(format_load("gc_stw_load", x.cpu_time_stw, x.cpu_time_tot));
	s.println(format_load("gc_stw_load", y.cpu_time_stw, y.cpu_time_tot, (rate_y - rate_x) * 100.0));

	rate_x = x.cpu_time_cgc / x.cpu_time_tot;
	rate_y = y.cpu_time_cgc / y.cpu_time_tot;
	s.println(format_load("gc_concurrent_load", x.cpu_time_cgc, x.cpu_time_tot));
	s.println(format_load("gc_concurrent_load", y.cpu_time_cgc, y.cpu_time_tot, (rate_y - rate_x) * 100.0));

	rate_x = (x.cpu_time_stw + x.cpu_time_cgc) / x.cpu_time_tot;
	rate_y = (y.cpu_time_stw + y.cpu_time_cgc) / y.cpu_time_tot;
	s.println(format_load("gc_load", x.cpu_time_stw + x.cpu_time_cgc, x.cpu_time_tot));
	s.println(format_load("gc_load", y.cpu_time_stw + y.cpu_time_cgc, y.cpu_time_tot, (rate_y - rate_x) * 100.0));
    }

    public void print_rates(PrintStream s)
    {
	print_rates(s, new RateVars(this));
    }

    public void print(PrintStream s)
    {
	s.println(hdr);
	print_stats(s);
	if (has_generated_timestamps())
	    return;
	s.println();
	print_rates(s);
    }

    public static void print_comparison(PrintStream s, String name, Stats ref_stats, Stats new_stats, boolean terse)
    {
	// Print only if there is at least one data point.
	if (ref_stats.n() + new_stats.n() > 0)
	{
	    if (!terse)
	    {
		print(s, name, ref_stats);
		print(s, name, new_stats);
	    }
	    if (ref_stats.n() > 0 && new_stats.n() > 0)
	    {
		s.println(format_percent_change(name, ref_stats, new_stats));
	    }
	}
    }

    public void print_comparison(PrintStream s, GCStats that, boolean terse)
    {
	s.println(hdr);
	for (GCMetric metric : GCMetric.values())
	{
	    if (enabled(metric) && that.enabled(metric))
	    {
		print_comparison(s, metric.name(), stats(metric), that.stats(metric), terse);
	    }
	}

	if (this.has_generated_timestamps() || that.has_generated_timestamps())
	    return;

	s.println();
	print_comparison(s, new RateVars(this), new RateVars(that));
    }

    public void print_comparison(PrintStream s, GCStats that)
    {
	print_comparison(s, that, false);
    }

    public void save(String prefix, String suffix) throws IOException
    {
	/* empty */
    }

    public void addStopAppTime(double stopTime)
    {
	if (maxAppStoppedTime < stopTime)
	{
	    maxAppStoppedTime = stopTime;
	}

	stopTimeList.add(stopTime);
	sumAppStoppedTime += stopTime;
    }

    public String getMaxAppStoppedTime()
    {
	return String.valueOf(doubleformat.format(maxAppStoppedTime)) + "s";
    }

    public String getAvgStopTime()
    {
	return String.valueOf(doubleformat.format((sumAppStoppedTime / gcLastLogTime) * 1000)) + "ms/m";
    }

    public void addYoungGCLog(double timestampBegin, double pauseTime, double yongGenerationBeginSize, double yongGenerationEndSize)
    {
	if (gcLastLogTime < timestampBegin + pauseTime)
	{
	    gcLastLogTime = timestampBegin + pauseTime;
	}

	minorGCConsumeTimes.put(timestampBegin, pauseTime);
	minorGCMemoryChanges.put(timestampBegin, new Double[]
	{ yongGenerationBeginSize, yongGenerationEndSize });
    }

    public void addConcMarkSweepGCLog(double timestampBegin, double pauseTime, double oldGenerationBeginSize, double oldGenerationEndSize)
    {
	if (gcLastLogTime < timestampBegin + pauseTime)
	{
	    gcLastLogTime = timestampBegin + pauseTime;
	}

	cmsGCConsumeTimes.put(timestampBegin, pauseTime);
	cmsGCMemoryChanges.put(timestampBegin, new Double[]
	{ oldGenerationBeginSize, oldGenerationEndSize });
    }

    public void addFullGCLog(double timestampBegin, double pauseTime, double beginSize, double endSize)
    {
	if (gcLastLogTime < timestampBegin + pauseTime)
	{
	    gcLastLogTime = timestampBegin + pauseTime;
	}

	fullGCConsumeTimes.put(timestampBegin, pauseTime);
	fullGCMemoryChanges.put(timestampBegin, new Double[]
	{ beginSize, endSize });
    }

    public Map<Double, Double> getMinorGCConsumeTimes()
    {
	return minorGCConsumeTimes;
    }

    public void setMinorGCConsumeTimes(Map<Double, Double> minorGCConsumeTimes)
    {
	this.minorGCConsumeTimes = minorGCConsumeTimes;
    }

    public Map<Double, Double[]> getCmsGCMemoryChanges()
    {
	return cmsGCMemoryChanges;
    }

    public void setCmsGCMemoryChanges(Map<Double, Double[]> cmsGCMemoryChanges)
    {
	this.cmsGCMemoryChanges = cmsGCMemoryChanges;
    }

    public Map<Double, Double[]> getMinorGCMemoryChanges()
    {
	return minorGCMemoryChanges;
    }

    public Map<Double, Double> getFullGCConsumeTimes()
    {
	return fullGCConsumeTimes;
    }

    public Map<Double, Double[]> getFullGCMemoryChanges()
    {
	return fullGCMemoryChanges;
    }

    public Map<Double, Double> getCmsGCConsumeTimes()
    {
	return cmsGCConsumeTimes;
    }

    public String getSumAppStoppedTime()
    {
	return String.valueOf(doubleformat.format(sumAppStoppedTime)) + "s";
    }

    public int getMinorGCCount()
    {
	return minorGCConsumeTimes.size();
    }

    public String getMinorGCTotalConsumeTime()
    {
	double totalTime = 0.0;
	for (Entry<Double, Double> entry : minorGCConsumeTimes.entrySet())
	{
	    totalTime += entry.getValue();
	}

	return String.valueOf(doubleformat.format(totalTime)) + "s";
    }

    public String getAvgMinorGCConsumeTime()
    {
	double totalTime = 0.0;
	for (Entry<Double, Double> entry : minorGCConsumeTimes.entrySet())
	{
	    totalTime += entry.getValue();
	}

	return String.valueOf(doubleformat.format(totalTime / minorGCConsumeTimes.size())) + "s";
    }

    public int getFullGCCount()
    {
	return fullGCConsumeTimes.size();
    }

    public String getFullGCConsumeTime()
    {
	double totalTime = 0.0;
	for (Double cusumeTime : fullGCConsumeTimes.values())
	{
	    totalTime += cusumeTime;
	}

	return String.valueOf(doubleformat.format(totalTime)) + "s";
    }

    public String getAvgFullGCConsumeTime()
    {
	double totalTime = 0.0;
	for (Double cusumeTime : fullGCConsumeTimes.values())
	{
	    totalTime += cusumeTime;
	}

	return String.valueOf(doubleformat.format(totalTime / fullGCConsumeTimes.size())) + "s";
    }

    public String getGCLastLogTime()
    {
	return String.valueOf(gcLastLogTime) + "s";
    }
    
    public double getGCLogTime()
    {
	return gcLastLogTime;
    }

    public String getThroughput()
    {
	return String.valueOf(doubleformat.format((sumAppStoppedTime / gcLastLogTime) * 100) + "%");
    }

    public void promotionFailedIncrement()
    {
	cmsGCPromotionFailedCount++;
    }

    public void concurrentFailedIncrement()
    {
	cmsGCConcurrentModeFailedCount++;
    }

    public String getGCType()
    {
	if(gcType==GCType.CONC_MARK_SWEEP_GC)
	{
	    return "ConcMarkSweepGC";
	}else if(gcType==GCType.PARALLEL_GC)
	{
	    return "Parallel GC";
	}else if(gcType == GCType.SERIAL_GC)
	{
	    return "Serial GC";
	}
	
	return "unkown GC Type";
    }

    public GCType getGcType()
    {
        return gcType;
    }

    public void setGcType(GCType gcType)
    {
        this.gcType = gcType;
    }

    public int getCmsGCPromotionFailedCount()
    {
        return cmsGCPromotionFailedCount;
    }

    public int getCmsGCConcurrentModeFailedCount()
    {
        return cmsGCConcurrentModeFailedCount;
    }

}
