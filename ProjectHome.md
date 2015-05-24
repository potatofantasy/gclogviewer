I'm not familiar with swt,if you're swt programmer and interested this project,welcome attend this project.
# Current #
A free open source tool to visualize data produced by the Java VM options `-Xloggc:<file>`,now just supported jdk 1.6,it shows the gc trend in one graph with multi points of view,the view concludes: <br>
<ol><li>GC Summary Info: GC Type,Throughput,YGC Info,FGC Info,CMS GC Info<br>
</li><li>GC Trend<br>
<ul><li>YGC/FGC/CMS GC happenTime and PauseTime;<br>
</li></ul></li><li>Memory Trend<br>
<ul><li>YGC/FGC/CMS GC happenTime and Memory Before GC and Memory After GC;<br>
</li></ul></li><li>Memory Leak detection<br>
<ul><li>based on Old Generation Size change to detect memory leak;<br>
</li></ul></li><li>GC Tuning<br>
<ul><li>provide datas for gc tuning,such as ygc lds/fgc lds/promotion to old size trend;<br>
</li></ul></li><li>Compare Log<br>
<ul><li>so u can see tuning result.</li></ul></li></ol>

On the trend graph you can save or zoom to see details.<br>
<br>
<h1>Quick Start</h1>

<ul><li>Add <code>-Xloggc:&lt;file&gt; -XX:+PrintGCDetails -XX:+PrintGCTimeStamps || -XX:+PrintGCDateStamps</code> options to your java app startup script;<br>
</li><li>Download the latest gclogviewer from <a href='http://code.google.com/p/gclogviewer/downloads/list'>http://code.google.com/p/gclogviewer/downloads/list</a> ,then unzip or tar xfz;<br>
</li><li>Run run.bat or run.sh,then select file->open log file,then you can see the result.</li></ul>

<h1>ScreenShot</h1>

<h2>Main View</h2>
<a href='http://gclogviewer.googlecode.com/files/gclogviewer_b.PNG'>http://gclogviewer.googlecode.com/files/gclogviewer_s.PNG</a>

<h1>Feedback</h1>

If you have question or advice,pls visit <a href='http://code.google.com/p/gclogviewer/w/list'>http://code.google.com/p/gclogviewer/w/list</a> and add to it.<br>
<br>
<h1>Future</h1>
Above all is the gclogviewer 0.3 features,in next version(0.4) we'all bring the following features:<br>
<ol><li>Improve analyze and ui performance,now trend graph is a little slow;<br>
</li><li>GC Tuning advice;<br>
</li><li>Export viewer info to pdf and excel;<br>
</li><li>Support more jdk version: 1.5 and 7;<br>
</li><li>Web version.