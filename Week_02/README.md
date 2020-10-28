学习笔记


GC 调优，关注两个目标：

响应速度（Responsiveness）：响应速度指程序或系统对一个请求的响应有多迅速。 比如，用户订单查询响应时间，对响应速度要求很高的系统，较大的停顿时间是不可接受的。调优的重点是在短的时间内快速响应。

吞吐量（Throughput）：吞吐量关注在一个特定时间段内应用系统的大工作量。 例如每小时批处理系统能完成的任务数量，在吞吐量方面优化的系统，较长的 GC 停顿时间也是可以接受的，因为高吞吐量应用更关心的是如何尽可能快地完成整个任务，不考虑快速响应用户请求。
GC 调优中，GC 导致的应用暂停时间影响系统响应速度，GC 处理线程的 CPU 使用率影响系统吞吐量

GC 事件分类

根据垃圾收集回收的区域不同，垃圾收集主要分为：
Young GC
Old GC
Full GC
Mixed GC

1）Young GC

新生代内存的垃圾收集事件称为 Young GC（又称 Minor GC），当 JVM 无法为新对象分配在新生代内存空间时总会触发 Young GC。

比如 Eden 区占满时，新对象分配频率越高，Young GC 的频率就越高。

Young GC 每次都会引起全线停顿（Stop-The-World），暂停所有的应用线程，停顿时间相对老年代 GC 造成的停顿，几乎可以忽略不计。

2）Old GC 、Full GC、Mixed GC

Old GC：只清理老年代空间的 GC 事件，只有 CMS 的并发收集是这个模式。

Full GC：清理整个堆的 GC 事件，包括新生代、老年代、元空间等 。

Mixed GC：清理整个新生代以及部分老年代的 GC，只有 G1 有这个模式。


cms gc
并发模式失败（concurrent mode failure）：当 CMS 在执行回收时，新生代发生垃圾回收，同时老年代又没有足够的空间容纳晋升的对象时，CMS 垃圾回收就会退化成单线程的 Full GC。所有的应用线程都会被暂停，老年代中所有的无效对象都被回收
晋升失败（promotion failed）：当新生代发生垃圾回收，老年代有足够的空间可以容纳晋升的对象，但是由于空闲空间的碎片化，导致晋升失败，此时会触发单线程且带压缩动作的 Full GC
并发模式失败和晋升失败都会导致长时间的停顿，常见解决思路如下：
1、降低触发 CMS GC 的阈值。 即参数 -XX:CMSInitiatingOccupancyFraction 的值，让 CMS GC 尽早执行，以保证有足够的空间。
2、增加 CMS 线程数，即参数 -XX:ConcGCThreads。
3、增大老年代空间。
4、让对象尽量在新生代回收，避免进入老年代


G1 GC
G1正常处理流程中没有 Full GC，只有在垃圾回收处理不过来（或者主动触发）时才会出现，G1 的 Full GC 就是单线程执行的 Serial old gc，会导致很长的 STW，是调优的重点，需要尽量避免 Full GC

G1不要设置 Young 区的大小
原因是为了尽量满足目标停顿时间，逻辑上的 Young 区会进行动态调整。如果设置了大小，则会覆盖掉并且会禁用掉对停顿时间的控制

GC 优化的核心思路在于：尽可能让对象在新生代中分配和回收，尽量避免过多对象进入老年代，导致对老年代频繁进行垃圾回收，同时给系统足够的内存减少新生代垃圾回收次数，进行系统分析和优化也是围绕着这个思路展开

user 表示所有GC线程消耗的CPU时间； 
sys  表示系统调用和系统等待事件消耗的时间。 
real 表示应用程序暂停的时间。


串行GC

并行GC

CMS GC

G1 GC

Serial GC 随着Xmx堆内存设置增大，效率逐渐降低，大于1g，GC时间较长 吞吐量低
Xmx设置过小，易发生OutOfMemmoryErr
Xmx堆内存设置较大时(4g)，G1效率比价好，GC时间较短  G1 > CMS > Parall GC
Xmx堆内存设置较小时（512m） 串行GC效率较高

不管选取哪种GC，堆内存Xmx设置过小，容易发生内存溢出错误
当堆内存Xmx和Xms设置较小时，发生young gc和full gc次数较多，串行GC相对性能较好gc次数少，暂停时间短,可选取串行GC使用
当增大堆内存，GC次数变少，直到堆内存较大时，不发生old gc 以及full gc mix gc
当堆内存较大时，暂停时间较长，并行gc暂停时间较长，延迟高 G1 Parrallel CMS Serial

低延迟：PrallNew + CMS Old
高吞吐：Parrallel


环境配置：2c 8g

一、串行GC

Allocation Failure：表示向young generation(Eden) 给新对象申请空间，但是young generation(Eden)剩余的合适空间不够所需的大小导致的minor gc
Full GC(Ergonmics) 默认开启UseAdaptiveSizePolicy，JVM进行自适应调整引发的full gc
DefNew： 表示垃圾收集器的名称。这个名字表示：年轻代使用的单线程、 标记­复制、STW 垃圾收集器
Tenured：用于清理老年代空间的垃圾收集器名称。 Tenured 表明使用的是单线程的STW垃圾收集器，使用的算法为标记‐清除‐整理(mark‐sweepcompact)


不设置Xmx
java -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T21:11:28.548-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.548-0800: [DefNew: 34619K->4352K(39296K), 0.0172217 secs] 34619K->13477K(126720K), 0.0172986 secs] [Times: user=0.01 sys=0.01, real=0.02 secs] 
2020-10-26T21:11:28.579-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.579-0800: [DefNew: 39243K->4343K(39296K), 0.0168662 secs] 48369K->24745K(126720K), 0.0170170 secs] [Times: user=0.00 sys=0.00, real=0.02 secs] 
2020-10-26T21:11:28.612-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.612-0800: [DefNew: 39045K->4350K(39296K), 0.0149896 secs] 59447K->35994K(126720K), 0.0150383 secs] [Times: user=0.00 sys=0.01, real=0.01 secs] 
2020-10-26T21:11:28.643-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.643-0800: [DefNew: 39294K->4350K(39296K), 0.0376401 secs] 70938K->55796K(126720K), 0.0376864 secs] [Times: user=0.01 sys=0.01, real=0.04 secs] 
2020-10-26T21:11:28.692-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.692-0800: [DefNew: 39043K->4344K(39296K), 0.0094384 secs] 90489K->66151K(126720K), 0.0094881 secs] [Times: user=0.01 sys=0.01, real=0.01 secs] 
2020-10-26T21:11:28.709-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.709-0800: [DefNew: 39036K->4345K(39296K), 0.0113518 secs] 100843K->79007K(126720K), 0.0114030 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
2020-10-26T21:11:28.728-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.728-0800: [DefNew: 39289K->4345K(39296K), 0.0137181 secs]2020-10-26T21:11:28.742-0800: [Tenured: 87822K->87224K(88000K), 0.0153345 secs] 113951K->87224K(127296K), [Metaspace: 2719K->2719K(1056768K)], 0.0293203 secs] [Times: user=0.02 sys=0.01, real=0.03 secs] 
2020-10-26T21:11:28.781-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.781-0800: [DefNew: 58240K->7231K(65472K), 0.0277715 secs] 145464K->111054K(210848K), 0.0278175 secs] [Times: user=0.01 sys=0.02, real=0.02 secs] 
2020-10-26T21:11:28.822-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.822-0800: [DefNew: 64823K->7229K(65472K), 0.0228677 secs] 168646K->125434K(210848K), 0.0229200 secs] [Times: user=0.01 sys=0.01, real=0.02 secs] 
2020-10-26T21:11:28.856-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.856-0800: [DefNew: 65469K->7230K(65472K), 0.0192516 secs] 183674K->142857K(210848K), 0.0192992 secs] [Times: user=0.01 sys=0.01, real=0.02 secs] 
2020-10-26T21:11:28.891-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.892-0800: [DefNew: 65470K->7230K(65472K), 0.0171183 secs]2020-10-26T21:11:28.909-0800: [Tenured: 155543K->146842K(155644K), 0.0342450 secs] 201097K->146842K(221116K), [Metaspace: 2719K->2719K(1056768K)], 0.0516629 secs] [Times: user=0.04 sys=0.00, real=0.05 secs] 
2020-10-26T21:11:28.977-0800: [GC (Allocation Failure) 2020-10-26T21:11:28.977-0800: [DefNew: 97683K->12219K(110208K), 0.0178512 secs] 244526K->172902K(354948K), 0.0178918 secs] [Times: user=0.01 sys=0.01, real=0.02 secs] 
2020-10-26T21:11:29.019-0800: [GC (Allocation Failure) 2020-10-26T21:11:29.019-0800: [DefNew: 110203K->12222K(110208K), 0.0421423 secs] 270886K->204667K(354948K), 0.0421845 secs] [Times: user=0.01 sys=0.02, real=0.05 secs] 
2020-10-26T21:11:29.078-0800: [GC (Allocation Failure) 2020-10-26T21:11:29.078-0800: [DefNew: 110206K->12223K(110208K), 0.0271193 secs] 302651K->234902K(354948K), 0.0271632 secs] [Times: user=0.01 sys=0.01, real=0.03 secs] 
2020-10-26T21:11:29.128-0800: [GC (Allocation Failure) 2020-10-26T21:11:29.128-0800: [DefNew: 110070K->12222K(110208K), 0.0295954 secs]2020-10-26T21:11:29.158-0800: [Tenured: 252458K->212097K(252644K), 0.0458901 secs] 332749K->212097K(362852K), [Metaspace: 2720K->2720K(1056768K)], 0.0758206 secs] [Times: user=0.06 sys=0.02, real=0.08 secs] 
2020-10-26T21:11:29.247-0800: [GC (Allocation Failure) 2020-10-26T21:11:29.247-0800: [DefNew: 141440K->17662K(159104K), 0.0234671 secs] 353537K->259555K(512600K), 0.0235203 secs] [Times: user=0.01 sys=0.01, real=0.03 secs] 
2020-10-26T21:11:29.296-0800: [GC (Allocation Failure) 2020-10-26T21:11:29.296-0800: [DefNew: 158850K->17663K(159104K), 0.0494063 secs] 400743K->301813K(512600K), 0.0494520 secs] [Times: user=0.03 sys=0.02, real=0.05 secs] 
2020-10-26T21:11:29.376-0800: [GC (Allocation Failure) 2020-10-26T21:11:29.376-0800: [DefNew: 159019K->17662K(159104K), 0.0608679 secs] 443169K->343533K(512600K), 0.0609104 secs] [Times: user=0.03 sys=0.02, real=0.06 secs] 
2020-10-26T21:11:29.465-0800: [GC (Allocation Failure) 2020-10-26T21:11:29.465-0800: [DefNew: 159102K->17662K(159104K), 0.0695257 secs]2020-10-26T21:11:29.535-0800: [Tenured: 370385K->274941K(370464K), 0.0667098 secs] 484973K->274941K(529568K), [Metaspace: 2720K->2720K(1056768K)], 0.1365263 secs] [Times: user=0.09 sys=0.02, real=0.14 secs] 
执行结束!共生成对象次数:5418
Heap
 def new generation   total 206272K, used 8019K [0x0000000740000000, 0x000000074dfd0000, 0x000000076aaa0000)
  eden space 183360K,   4% used [0x0000000740000000, 0x00000007407d4fb8, 0x000000074b310000)
  from space 22912K,   0% used [0x000000074b310000, 0x000000074b310000, 0x000000074c970000)
  to   space 22912K,   0% used [0x000000074c970000, 0x000000074c970000, 0x000000074dfd0000)
 tenured generation   total 458236K, used 274941K [0x000000076aaa0000, 0x0000000786a1f000, 0x00000007c0000000)
   the space 458236K,  59% used [0x000000076aaa0000, 0x000000077b71f488, 0x000000077b71f600, 0x0000000786a1f000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K



Xmx设置128m 只发生了young gc和full gc，并发生了OutOfMemoryError
java -Xmx128m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T21:26:02.044-0800: [GC (Allocation Failure) 2020-10-26T21:26:02.044-0800: [DefNew: 34944K->4352K(39296K), 0.0101244 secs] 34944K->13798K(126720K), 0.0102953 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.070-0800: [GC (Allocation Failure) 2020-10-26T21:26:02.070-0800: [DefNew: 39018K->4349K(39296K), 0.0112001 secs] 48465K->24344K(126720K), 0.0112699 secs] [Times: user=0.00 sys=0.01, real=0.01 secs] 
2020-10-26T21:26:02.100-0800: [GC (Allocation Failure) 2020-10-26T21:26:02.100-0800: [DefNew: 38924K->4342K(39296K), 0.0099837 secs] 58919K->37463K(126720K), 0.0100279 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.117-0800: [GC (Allocation Failure) 2020-10-26T21:26:02.117-0800: [DefNew: 38931K->4336K(39296K), 0.0116108 secs] 72052K->51771K(126720K), 0.0116545 secs] [Times: user=0.00 sys=0.01, real=0.01 secs] 
2020-10-26T21:26:02.137-0800: [GC (Allocation Failure) 2020-10-26T21:26:02.137-0800: [DefNew: 39136K->4344K(39296K), 0.0090127 secs] 86571K->61192K(126720K), 0.0090774 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.155-0800: [GC (Allocation Failure) 2020-10-26T21:26:02.155-0800: [DefNew: 39162K->4343K(39296K), 0.0085929 secs] 96010K->72783K(126720K), 0.0086339 secs] [Times: user=0.00 sys=0.01, real=0.01 secs] 
2020-10-26T21:26:02.171-0800: [GC (Allocation Failure) 2020-10-26T21:26:02.171-0800: [DefNew: 39040K->4350K(39296K), 0.0112313 secs] 107481K->88506K(126720K), 0.0112785 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.189-0800: [GC (Allocation Failure) 2020-10-26T21:26:02.189-0800: [DefNew: 39294K->39294K(39296K), 0.0000183 secs]2020-10-26T21:26:02.189-0800: [Tenured: 84155K->87111K(87424K), 0.0217281 secs] 123450K->95499K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0218048 secs] [Times: user=0.02 sys=0.00, real=0.03 secs] 
2020-10-26T21:26:02.219-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.219-0800: [Tenured: 87111K->87238K(87424K), 0.0174569 secs] 126237K->102945K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0175044 secs] [Times: user=0.02 sys=0.01, real=0.02 secs] 
2020-10-26T21:26:02.242-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.242-0800: [Tenured: 87346K->86704K(87424K), 0.0193607 secs] 126613K->105167K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0194111 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
2020-10-26T21:26:02.268-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.268-0800: [Tenured: 87245K->87404K(87424K), 0.0245489 secs] 126392K->103118K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0246031 secs] [Times: user=0.03 sys=0.00, real=0.03 secs] 
2020-10-26T21:26:02.297-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.297-0800: [Tenured: 87404K->87404K(87424K), 0.0049659 secs] 126662K->112284K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0050209 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.305-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.305-0800: [Tenured: 87404K->87404K(87424K), 0.0061318 secs] 126509K->117567K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0061866 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.313-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.313-0800: [Tenured: 87404K->87404K(87424K), 0.0029609 secs] 126643K->121961K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0030529 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.318-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.318-0800: [Tenured: 87404K->87338K(87424K), 0.0238037 secs] 126654K->120231K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0238548 secs] [Times: user=0.03 sys=0.00, real=0.03 secs] 
2020-10-26T21:26:02.343-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.343-0800: [Tenured: 87338K->87338K(87424K), 0.0058184 secs] 126562K->123939K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0058663 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.349-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.349-0800: [Tenured: 87338K->87338K(87424K), 0.0026297 secs] 126235K->123982K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0026886 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.353-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.353-0800: [Tenured: 87338K->87338K(87424K), 0.0029062 secs] 126202K->125157K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0029808 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.356-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.356-0800: [Tenured: 87338K->87299K(87424K), 0.0173910 secs] 126495K->124493K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0174346 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
2020-10-26T21:26:02.374-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.374-0800: [Tenured: 87299K->87299K(87424K), 0.0026603 secs] 126577K->124943K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0027081 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.378-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.378-0800: [Tenured: 87387K->87387K(87424K), 0.0044770 secs] 126675K->125620K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0045602 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.383-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.383-0800: [Tenured: 87387K->87387K(87424K), 0.0018527 secs] 126058K->125675K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0018894 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.385-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.385-0800: [Tenured: 87387K->87236K(87424K), 0.0169600 secs] 126511K->124870K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0170147 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
2020-10-26T21:26:02.403-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.403-0800: [Tenured: 87236K->87236K(87424K), 0.0027261 secs] 126289K->125143K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0027748 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.406-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.406-0800: [Tenured: 87236K->87236K(87424K), 0.0029049 secs] 126350K->126259K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0029522 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.409-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.409-0800: [Tenured: 87236K->87236K(87424K), 0.0042410 secs] 126521K->126229K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0043064 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.413-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.413-0800: [Tenured: 87236K->87163K(87424K), 0.0180978 secs] 126229K->125946K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0181428 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
2020-10-26T21:26:02.432-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.432-0800: [Tenured: 87163K->87163K(87424K), 0.0017270 secs] 126396K->125998K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0017619 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.434-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.434-0800: [Tenured: 87163K->86973K(87424K), 0.0077250 secs] 125998K->125808K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0077602 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.442-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.442-0800: [Tenured: 87398K->87398K(87424K), 0.0069652 secs] 126658K->126350K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0070101 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.449-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.449-0800: [Tenured: 87398K->86973K(87424K), 0.0024812 secs] 126592K->126032K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0025265 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
2020-10-26T21:26:02.452-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.452-0800: [Tenured: 87356K->87356K(87424K), 0.0021556 secs] 126617K->126283K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0021928 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:26:02.454-0800: [Full GC (Allocation Failure) 2020-10-26T21:26:02.454-0800: [Tenured: 87356K->87356K(87424K), 0.0014374 secs] 126283K->126283K(126720K), [Metaspace: 2719K->2719K(1056768K)], 0.0014660 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
	at GCLogAnalysis.generateGarbage(GCLogAnalysis.java:42)
	at GCLogAnalysis.main(GCLogAnalysis.java:25)
Heap
 def new generation   total 39296K, used 39033K [0x00000007b8000000, 0x00000007baaa0000, 0x00000007baaa0000)
  eden space 34944K, 100% used [0x00000007b8000000, 0x00000007ba220000, 0x00000007ba220000)
  from space 4352K,  93% used [0x00000007ba660000, 0x00000007baa5e5a8, 0x00000007baaa0000)
  to   space 4352K,   0% used [0x00000007ba220000, 0x00000007ba220000, 0x00000007ba660000)
 tenured generation   total 87424K, used 87356K [0x00000007baaa0000, 0x00000007c0000000, 0x00000007c0000000)
   the space 87424K,  99% used [0x00000007baaa0000, 0x00000007bffef3d0, 0x00000007bffef400, 0x00000007c0000000)
 Metaspace       used 2750K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 299K, capacity 386K, committed 512K, reserved 1048576K




java -Xmx512m -Xms512m  -XX:+UseSerialGC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
发生了9次young gc 和 4次 full gc
正在执行...
2020-10-26T22:29:39.847-0800: [GC (Allocation Failure)  139776K->42057K(506816K), 0.0284205 secs]
2020-10-26T22:29:39.903-0800: [GC (Allocation Failure)  181833K->84085K(506816K), 0.0444959 secs]
2020-10-26T22:29:39.971-0800: [GC (Allocation Failure)  223861K->128418K(506816K), 0.0373103 secs]
2020-10-26T22:29:40.034-0800: [GC (Allocation Failure)  268194K->179079K(506816K), 0.0427518 secs]
2020-10-26T22:29:40.100-0800: [GC (Allocation Failure)  318574K->227087K(506816K), 0.0439242 secs]
2020-10-26T22:29:40.169-0800: [GC (Allocation Failure)  366863K->270476K(506816K), 0.0401783 secs]
2020-10-26T22:29:40.235-0800: [GC (Allocation Failure)  410252K->317029K(506816K), 0.0601893 secs]
2020-10-26T22:29:40.324-0800: [Full GC (Allocation Failure)  456703K->265449K(506816K), 0.0641739 secs]
2020-10-26T22:29:40.411-0800: [GC (Allocation Failure)  405225K->307469K(506816K), 0.0082690 secs]
2020-10-26T22:29:40.442-0800: [GC (Allocation Failure)  447245K->352292K(506816K), 0.0701784 secs]
2020-10-26T22:29:40.541-0800: [Full GC (Allocation Failure)  492068K->311656K(506816K), 0.0590256 secs]
2020-10-26T22:29:40.623-0800: [Full GC (Allocation Failure)  451432K->317545K(506816K), 0.0562805 secs]
2020-10-26T22:29:40.702-0800: [Full GC (Allocation Failure)  456728K->309744K(506816K), 0.0614659 secs]
执行结束!共生成对象次数:6827


java -Xmx1g -Xms1g  -XX:+UseSerialGC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
发生了7次young gc，没有full gc
正在执行...
2020-10-26T22:50:53.078-0800: [GC (Allocation Failure)  279616K->85952K(1013632K), 0.0585495 secs]
2020-10-26T22:50:53.193-0800: [GC (Allocation Failure)  365568K->164485K(1013632K), 0.0755811 secs]
2020-10-26T22:50:53.323-0800: [GC (Allocation Failure)  444101K->246128K(1013632K), 0.0639076 secs]
2020-10-26T22:50:53.437-0800: [GC (Allocation Failure)  525744K->329286K(1013632K), 0.0666892 secs]
2020-10-26T22:50:53.552-0800: [GC (Allocation Failure)  608902K->403354K(1013632K), 0.0604435 secs]
2020-10-26T22:50:53.663-0800: [GC (Allocation Failure)  682970K->480443K(1013632K), 0.0624923 secs]
2020-10-26T22:50:53.774-0800: [GC (Allocation Failure)  760044K->554489K(1013632K), 0.0699577 secs]
执行结束!共生成对象次数:8017


java -Xmx2g -Xms2g  -XX:+UseSerialGC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
发生了2次young gc
正在执行...
2020-10-26T22:49:49.414-0800: [GC (Allocation Failure)  559232K->137514K(2027264K), 0.1816368 secs]
2020-10-26T22:49:49.757-0800: [GC (Allocation Failure)  696746K->268790K(2027264K), 0.1580373 secs]


java -Xmx4g -Xms4g  -XX:+UseSerialGC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
发生了一次young gc
正在执行...
2020-10-26T22:30:07.100-0800: [GC (Allocation Failure)  1118528K->239076K(4054528K), 0.2739143 secs]




二、并行GC

java -Xmx512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T21:45:39.468-0800: [GC (Allocation Failure) [PSYoungGen: 32938K->5115K(38400K)] 32938K->10742K(125952K), 0.0072146 secs] [Times: user=0.01 sys=0.01, real=0.01 secs] 
2020-10-26T21:45:39.490-0800: [GC (Allocation Failure) [PSYoungGen: 38330K->5115K(71680K)] 43957K->23769K(159232K), 0.0083732 secs] [Times: user=0.01 sys=0.02, real=0.00 secs] 
2020-10-26T21:45:39.532-0800: [GC (Allocation Failure) [PSYoungGen: 71675K->5102K(71680K)] 90329K->45641K(159232K), 0.0141529 secs] [Times: user=0.01 sys=0.03, real=0.01 secs] 
2020-10-26T21:45:39.558-0800: [GC (Allocation Failure) [PSYoungGen: 71590K->5099K(138240K)] 112129K->70863K(225792K), 0.0169558 secs] [Times: user=0.02 sys=0.03, real=0.02 secs] 
2020-10-26T21:45:39.575-0800: [Full GC (Ergonomics) [PSYoungGen: 5099K->0K(138240K)] [ParOldGen: 65763K->67762K(138752K)] 70863K->67762K(276992K), [Metaspace: 2719K->2719K(1056768K)], 0.0147890 secs] [Times: user=0.04 sys=0.00, real=0.01 secs] 
2020-10-26T21:45:39.645-0800: [GC (Allocation Failure) [PSYoungGen: 133120K->5117K(138240K)] 200882K->112197K(276992K), 0.0212435 secs] [Times: user=0.02 sys=0.06, real=0.02 secs] 
2020-10-26T21:45:39.667-0800: [Full GC (Ergonomics) [PSYoungGen: 5117K->0K(138240K)] [ParOldGen: 107079K->100811K(196608K)] 112197K->100811K(334848K), [Metaspace: 2720K->2720K(1056768K)], 0.0193265 secs] [Times: user=0.05 sys=0.00, real=0.02 secs] 
2020-10-26T21:45:39.712-0800: [GC (Allocation Failure) [PSYoungGen: 133120K->36328K(95232K)] 233931K->155698K(291840K), 0.0259476 secs] [Times: user=0.02 sys=0.05, real=0.02 secs] 
2020-10-26T21:45:39.749-0800: [GC (Allocation Failure) [PSYoungGen: 95208K->51737K(116736K)] 214578K->171108K(313344K), 0.0134383 secs] [Times: user=0.04 sys=0.00, real=0.02 secs] 
2020-10-26T21:45:39.774-0800: [GC (Allocation Failure) [PSYoungGen: 110617K->57836K(116736K)] 229988K->184108K(313344K), 0.0147128 secs] [Times: user=0.02 sys=0.01, real=0.01 secs] 
2020-10-26T21:45:39.799-0800: [GC (Allocation Failure) [PSYoungGen: 116716K->46986K(116736K)] 242988K->201310K(313344K), 0.0250927 secs] [Times: user=0.04 sys=0.03, real=0.03 secs] 
2020-10-26T21:45:39.842-0800: [GC (Allocation Failure) [PSYoungGen: 105846K->39542K(116736K)] 260170K->220196K(313344K), 0.0218511 secs] [Times: user=0.03 sys=0.04, real=0.02 secs] 
2020-10-26T21:45:39.864-0800: [Full GC (Ergonomics) [PSYoungGen: 39542K->0K(116736K)] [ParOldGen: 180653K->183783K(283648K)] 220196K->183783K(400384K), [Metaspace: 2720K->2720K(1056768K)], 0.0347643 secs] [Times: user=0.08 sys=0.01, real=0.03 secs] 
2020-10-26T21:45:39.915-0800: [GC (Allocation Failure) [PSYoungGen: 58880K->18183K(116736K)] 242663K->201966K(400384K), 0.0031130 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
2020-10-26T21:45:39.929-0800: [GC (Allocation Failure) [PSYoungGen: 77063K->20750K(116736K)] 260846K->220384K(400384K), 0.0126153 secs] [Times: user=0.02 sys=0.02, real=0.02 secs] 
2020-10-26T21:45:39.954-0800: [GC (Allocation Failure) [PSYoungGen: 79024K->20905K(116736K)] 278658K->239984K(400384K), 0.0148728 secs] [Times: user=0.03 sys=0.02, real=0.01 secs] 
2020-10-26T21:45:39.981-0800: [GC (Allocation Failure) [PSYoungGen: 79785K->20681K(116736K)] 298864K->259753K(400384K), 0.0140294 secs] [Times: user=0.02 sys=0.02, real=0.01 secs] 
2020-10-26T21:45:40.011-0800: [GC (Allocation Failure) [PSYoungGen: 79561K->20709K(116736K)] 318633K->280301K(400384K), 0.0143043 secs] [Times: user=0.02 sys=0.03, real=0.01 secs] 
2020-10-26T21:45:40.025-0800: [Full GC (Ergonomics) [PSYoungGen: 20709K->0K(116736K)] [ParOldGen: 259591K->227062K(330240K)] 280301K->227062K(446976K), [Metaspace: 2720K->2720K(1056768K)], 0.0454240 secs] [Times: user=0.11 sys=0.00, real=0.05 secs] 
2020-10-26T21:45:40.081-0800: [GC (Allocation Failure) [PSYoungGen: 58729K->21812K(116736K)] 285792K->248875K(446976K), 0.0044085 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:45:40.100-0800: [GC (Allocation Failure) [PSYoungGen: 80684K->16385K(116736K)] 307747K->263605K(446976K), 0.0064030 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:45:40.118-0800: [GC (Allocation Failure) [PSYoungGen: 75124K->21332K(116736K)] 322344K->284616K(446976K), 0.0087294 secs] [Times: user=0.01 sys=0.01, real=0.01 secs] 
2020-10-26T21:45:40.141-0800: [GC (Allocation Failure) [PSYoungGen: 80212K->16462K(116736K)] 343496K->299452K(446976K), 0.0131066 secs] [Times: user=0.01 sys=0.02, real=0.01 secs] 
2020-10-26T21:45:40.166-0800: [GC (Allocation Failure) [PSYoungGen: 75342K->22978K(116736K)] 358332K->321196K(446976K), 0.0110633 secs] [Times: user=0.02 sys=0.01, real=0.01 secs] 
2020-10-26T21:45:40.190-0800: [GC (Allocation Failure) [PSYoungGen: 81769K->18694K(77824K)] 379987K->339291K(408064K), 0.0143651 secs] [Times: user=0.01 sys=0.03, real=0.01 secs] 
2020-10-26T21:45:40.205-0800: [Full GC (Ergonomics) [PSYoungGen: 18694K->0K(77824K)] [ParOldGen: 320596K->270597K(349696K)] 339291K->270597K(427520K), [Metaspace: 2720K->2720K(1056768K)], 0.0507197 secs] [Times: user=0.12 sys=0.00, real=0.05 secs] 
2020-10-26T21:45:40.265-0800: [GC (Allocation Failure) [PSYoungGen: 58292K->26143K(117760K)] 328890K->296741K(467456K), 0.0048004 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
2020-10-26T21:45:40.281-0800: [GC (Allocation Failure) [PSYoungGen: 87583K->22667K(115200K)] 358181K->319027K(464896K), 0.0083165 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:45:40.302-0800: [GC (Allocation Failure) [PSYoungGen: 84107K->23615K(117760K)] 380467K->342215K(467456K), 0.0127659 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
2020-10-26T21:45:40.315-0800: [Full GC (Ergonomics) [PSYoungGen: 23615K->0K(117760K)] [ParOldGen: 318599K->296971K(349696K)] 342215K->296971K(467456K), [Metaspace: 2720K->2720K(1056768K)], 0.0479841 secs] [Times: user=0.11 sys=0.01, real=0.05 secs] 
2020-10-26T21:45:40.378-0800: [GC (Allocation Failure) [PSYoungGen: 66539K->20506K(115712K)] 363510K->317478K(465408K), 0.0039774 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:45:40.393-0800: [GC (Allocation Failure) [PSYoungGen: 87066K->26123K(123392K)] 384038K->342300K(473088K), 0.0084572 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
2020-10-26T21:45:40.415-0800: [GC (Allocation Failure) [PSYoungGen: 102411K->24123K(121856K)] 418588K->365507K(471552K), 0.0189263 secs] [Times: user=0.02 sys=0.02, real=0.02 secs] 
2020-10-26T21:45:40.434-0800: [Full GC (Ergonomics) [PSYoungGen: 24123K->0K(121856K)] [ParOldGen: 341383K->309115K(349696K)] 365507K->309115K(471552K), [Metaspace: 2720K->2720K(1056768K)], 0.0553367 secs] [Times: user=0.13 sys=0.00, real=0.06 secs] 
执行结束!共生成对象次数:6545
Heap
 PSYoungGen      total 121856K, used 3109K [0x00000007b5580000, 0x00000007bfb80000, 0x00000007c0000000)
  eden space 76288K, 4% used [0x00000007b5580000,0x00000007b5889550,0x00000007ba000000)
  from space 45568K, 0% used [0x00000007ba000000,0x00000007ba000000,0x00000007bcc80000)
  to   space 44032K, 0% used [0x00000007bd080000,0x00000007bd080000,0x00000007bfb80000)
 ParOldGen       total 349696K, used 309115K [0x00000007a0000000, 0x00000007b5580000, 0x00000007b5580000)
  object space 349696K, 88% used [0x00000007a0000000,0x00000007b2ddefb8,0x00000007b5580000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K





  java -Xmx1024m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T21:47:09.067-0800: [GC (Allocation Failure) [PSYoungGen: 32870K->5119K(38400K)] 32870K->11081K(125952K), 0.0063263 secs] [Times: user=0.01 sys=0.01, real=0.01 secs] 
2020-10-26T21:47:09.089-0800: [GC (Allocation Failure) [PSYoungGen: 38399K->5116K(71680K)] 44361K->22226K(159232K), 0.0078933 secs] [Times: user=0.01 sys=0.02, real=0.01 secs] 
2020-10-26T21:47:09.131-0800: [GC (Allocation Failure) [PSYoungGen: 71676K->5113K(71680K)] 88786K->46300K(159232K), 0.0137364 secs] [Times: user=0.01 sys=0.03, real=0.01 secs] 
2020-10-26T21:47:09.158-0800: [GC (Allocation Failure) [PSYoungGen: 71659K->5108K(138240K)] 112846K->71785K(225792K), 0.0161809 secs] [Times: user=0.02 sys=0.03, real=0.02 secs] 
2020-10-26T21:47:09.174-0800: [Full GC (Ergonomics) [PSYoungGen: 5108K->0K(138240K)] [ParOldGen: 66677K->68655K(140800K)] 71785K->68655K(279040K), [Metaspace: 2719K->2719K(1056768K)], 0.0147106 secs] [Times: user=0.03 sys=0.01, real=0.01 secs] 
2020-10-26T21:47:09.246-0800: [GC (Allocation Failure) [PSYoungGen: 133120K->5112K(138240K)] 201775K->112036K(279040K), 0.0216720 secs] [Times: user=0.02 sys=0.05, real=0.02 secs] 
2020-10-26T21:47:09.268-0800: [Full GC (Ergonomics) [PSYoungGen: 5112K->0K(138240K)] [ParOldGen: 106924K->103410K(199168K)] 112036K->103410K(337408K), [Metaspace: 2720K->2720K(1056768K)], 0.0194511 secs] [Times: user=0.05 sys=0.00, real=0.02 secs] 
2020-10-26T21:47:09.317-0800: [GC (Allocation Failure) [PSYoungGen: 133120K->43730K(271872K)] 236530K->147141K(471040K), 0.0234887 secs] [Times: user=0.02 sys=0.05, real=0.03 secs] 
2020-10-26T21:47:09.430-0800: [GC (Allocation Failure) [PSYoungGen: 270034K->55801K(282112K)] 373445K->207088K(481280K), 0.0531736 secs] [Times: user=0.05 sys=0.12, real=0.05 secs] 
2020-10-26T21:47:09.484-0800: [Full GC (Ergonomics) [PSYoungGen: 55801K->0K(282112K)] [ParOldGen: 151286K->176900K(291328K)] 207088K->176900K(573440K), [Metaspace: 2720K->2720K(1056768K)], 0.0478270 secs] [Times: user=0.07 sys=0.02, real=0.05 secs] 
2020-10-26T21:47:09.582-0800: [GC (Allocation Failure) [PSYoungGen: 226304K->67070K(224768K)] 403204K->249401K(516096K), 0.0553391 secs] [Times: user=0.04 sys=0.02, real=0.05 secs] 
2020-10-26T21:47:09.668-0800: [GC (Allocation Failure) [PSYoungGen: 224766K->95742K(253440K)] 407097K->287350K(544768K), 0.0329161 secs] [Times: user=0.04 sys=0.01, real=0.04 secs] 
2020-10-26T21:47:09.730-0800: [GC (Allocation Failure) [PSYoungGen: 253438K->89269K(212480K)] 445046K->324783K(503808K), 0.0753971 secs] [Times: user=0.05 sys=0.04, real=0.07 secs] 
2020-10-26T21:47:09.805-0800: [Full GC (Ergonomics) [PSYoungGen: 89269K->0K(212480K)] [ParOldGen: 235513K->254100K(382464K)] 324783K->254100K(594944K), [Metaspace: 2720K->2720K(1056768K)], 0.0654099 secs] [Times: user=0.10 sys=0.02, real=0.07 secs] 
2020-10-26T21:47:09.892-0800: [GC (Allocation Failure) [PSYoungGen: 116736K->38218K(232960K)] 370836K->292318K(615424K), 0.0074383 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
2020-10-26T21:47:09.921-0800: [GC (Allocation Failure) [PSYoungGen: 154954K->42663K(227840K)] 409054K->330767K(610304K), 0.0435510 secs] [Times: user=0.04 sys=0.03, real=0.04 secs] 
2020-10-26T21:47:09.991-0800: [GC (Allocation Failure) [PSYoungGen: 159396K->45011K(230400K)] 447500K->371485K(612864K), 0.0680639 secs] [Times: user=0.03 sys=0.03, real=0.07 secs] 
2020-10-26T21:47:10.060-0800: [Full GC (Ergonomics) [PSYoungGen: 45011K->0K(230400K)] [ParOldGen: 326474K->287376K(425984K)] 371485K->287376K(656384K), [Metaspace: 2720K->2720K(1056768K)], 0.0526495 secs] [Times: user=0.13 sys=0.01, real=0.05 secs] 
执行结束!共生成对象次数:5979
Heap
 PSYoungGen      total 230400K, used 4910K [0x00000007aab00000, 0x00000007bf880000, 0x00000007c0000000)
  eden space 116736K, 4% used [0x00000007aab00000,0x00000007aafcb808,0x00000007b1d00000)
  from space 113664K, 0% used [0x00000007b1d00000,0x00000007b1d00000,0x00000007b8c00000)
  to   space 108544K, 0% used [0x00000007b8e80000,0x00000007b8e80000,0x00000007bf880000)
 ParOldGen       total 425984K, used 287376K [0x0000000780000000, 0x000000079a000000, 0x00000007aab00000)
  object space 425984K, 67% used [0x0000000780000000,0x00000007918a4190,0x000000079a000000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K




  java -Xmx2048m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T21:48:07.620-0800: [GC (Allocation Failure) [PSYoungGen: 33226K->5095K(38400K)] 33226K->12368K(125952K), 0.0069574 secs] [Times: user=0.00 sys=0.01, real=0.00 secs] 
2020-10-26T21:48:07.638-0800: [GC (Allocation Failure) [PSYoungGen: 38321K->5116K(71680K)] 45595K->23897K(159232K), 0.0128175 secs] [Times: user=0.01 sys=0.02, real=0.02 secs] 
2020-10-26T21:48:07.681-0800: [GC (Allocation Failure) [PSYoungGen: 71574K->5114K(71680K)] 90355K->41928K(159232K), 0.0147919 secs] [Times: user=0.01 sys=0.03, real=0.01 secs] 
2020-10-26T21:48:07.710-0800: [GC (Allocation Failure) [PSYoungGen: 71674K->5118K(138240K)] 108488K->66693K(225792K), 0.0167047 secs] [Times: user=0.01 sys=0.03, real=0.01 secs] 
2020-10-26T21:48:07.726-0800: [Full GC (Ergonomics) [PSYoungGen: 5118K->0K(138240K)] [ParOldGen: 61575K->62357K(129024K)] 66693K->62357K(267264K), [Metaspace: 2719K->2719K(1056768K)], 0.0120747 secs] [Times: user=0.03 sys=0.01, real=0.01 secs] 
2020-10-26T21:48:07.793-0800: [GC (Allocation Failure) [PSYoungGen: 133120K->5111K(138240K)] 195477K->107895K(267264K), 0.0233840 secs] [Times: user=0.02 sys=0.05, real=0.02 secs] 
2020-10-26T21:48:07.816-0800: [Full GC (Ergonomics) [PSYoungGen: 5111K->0K(138240K)] [ParOldGen: 102783K->101132K(192512K)] 107895K->101132K(330752K), [Metaspace: 2720K->2720K(1056768K)], 0.0194283 secs] [Times: user=0.04 sys=0.00, real=0.02 secs] 
2020-10-26T21:48:07.865-0800: [GC (Allocation Failure) [PSYoungGen: 133120K->46675K(278016K)] 234252K->147808K(470528K), 0.0271310 secs] [Times: user=0.02 sys=0.06, real=0.03 secs] 
2020-10-26T21:48:07.980-0800: [GC (Allocation Failure) [PSYoungGen: 277587K->56314K(287744K)] 378720K->216730K(480256K), 0.0595244 secs] [Times: user=0.05 sys=0.14, real=0.05 secs] 
2020-10-26T21:48:08.040-0800: [Full GC (Ergonomics) [PSYoungGen: 56314K->0K(287744K)] [ParOldGen: 160415K->183075K(296960K)] 216730K->183075K(584704K), [Metaspace: 2720K->2720K(1056768K)], 0.0332207 secs] [Times: user=0.07 sys=0.03, real=0.03 secs] 
2020-10-26T21:48:08.122-0800: [GC (Allocation Failure) [PSYoungGen: 231383K->68077K(493568K)] 414458K->251153K(790528K), 0.0321696 secs] [Times: user=0.03 sys=0.08, real=0.03 secs] 
2020-10-26T21:48:08.253-0800: [GC (Allocation Failure) [PSYoungGen: 465389K->101374K(498688K)] 648465K->344291K(795648K), 0.1583853 secs] [Times: user=0.07 sys=0.13, real=0.16 secs] 
2020-10-26T21:48:08.412-0800: [Full GC (Ergonomics) [PSYoungGen: 101374K->0K(498688K)] [ParOldGen: 242917K->265610K(397824K)] 344291K->265610K(896512K), [Metaspace: 2720K->2720K(1056768K)], 0.0657673 secs] [Times: user=0.10 sys=0.02, real=0.06 secs] 
2020-10-26T21:48:08.555-0800: [GC (Allocation Failure) [PSYoungGen: 397312K->119810K(526848K)] 662922K->385421K(924672K), 0.1887252 secs] [Times: user=0.05 sys=0.09, real=0.19 secs] 
执行结束!共生成对象次数:6384
Heap
 PSYoungGen      total 526848K, used 135714K [0x0000000795580000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 384512K, 4% used [0x0000000795580000,0x0000000796507de8,0x00000007acd00000)
  from space 142336K, 84% used [0x00000007b7500000,0x00000007bea00b50,0x00000007c0000000)
  to   space 157184K, 0% used [0x00000007acd00000,0x00000007acd00000,0x00000007b6680000)
 ParOldGen       total 397824K, used 265610K [0x0000000740000000, 0x0000000758480000, 0x0000000795580000)
  object space 397824K, 66% used [0x0000000740000000,0x0000000750362b08,0x0000000758480000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K




java -Xmx4096m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T21:48:26.684-0800: [GC (Allocation Failure) [PSYoungGen: 33253K->5115K(38400K)] 33253K->10386K(125952K), 0.0055283 secs] [Times: user=0.01 sys=0.01, real=0.00 secs] 
2020-10-26T21:48:26.704-0800: [GC (Allocation Failure) [PSYoungGen: 37931K->5119K(71680K)] 43202K->22607K(159232K), 0.0083797 secs] [Times: user=0.01 sys=0.02, real=0.01 secs] 
2020-10-26T21:48:26.749-0800: [GC (Allocation Failure) [PSYoungGen: 71679K->5101K(71680K)] 89167K->47836K(159232K), 0.0142659 secs] [Times: user=0.02 sys=0.03, real=0.02 secs] 
2020-10-26T21:48:26.776-0800: [GC (Allocation Failure) [PSYoungGen: 71590K->5119K(138240K)] 114325K->70397K(225792K), 0.0143396 secs] [Times: user=0.01 sys=0.03, real=0.02 secs] 
2020-10-26T21:48:26.790-0800: [Full GC (Ergonomics) [PSYoungGen: 5119K->0K(138240K)] [ParOldGen: 65278K->67824K(138752K)] 70397K->67824K(276992K), [Metaspace: 2719K->2719K(1056768K)], 0.0139972 secs] [Times: user=0.04 sys=0.00, real=0.01 secs] 
2020-10-26T21:48:26.861-0800: [GC (Allocation Failure) [PSYoungGen: 133120K->5118K(138240K)] 200944K->116393K(276992K), 0.0233424 secs] [Times: user=0.02 sys=0.06, real=0.02 secs] 
2020-10-26T21:48:26.885-0800: [Full GC (Ergonomics) [PSYoungGen: 5118K->0K(138240K)] [ParOldGen: 111274K->106384K(204288K)] 116393K->106384K(342528K), [Metaspace: 2720K->2720K(1056768K)], 0.0186788 secs] [Times: user=0.06 sys=0.00, real=0.02 secs] 
2020-10-26T21:48:26.933-0800: [GC (Allocation Failure) [PSYoungGen: 133120K->47964K(280064K)] 239504K->154349K(484352K), 0.0237204 secs] [Times: user=0.03 sys=0.05, real=0.02 secs] 
2020-10-26T21:48:27.046-0800: [GC (Allocation Failure) [PSYoungGen: 275804K->60404K(288256K)] 382189K->213848K(492544K), 0.0529844 secs] [Times: user=0.05 sys=0.12, real=0.05 secs] 
2020-10-26T21:48:27.099-0800: [Full GC (Ergonomics) [PSYoungGen: 60404K->0K(288256K)] [ParOldGen: 153444K->182367K(296960K)] 213848K->182367K(585216K), [Metaspace: 2720K->2720K(1056768K)], 0.0360095 secs] [Times: user=0.08 sys=0.04, real=0.04 secs] 
2020-10-26T21:48:27.185-0800: [GC (Allocation Failure) [PSYoungGen: 227809K->68446K(481280K)] 410177K->250814K(778240K), 0.0368762 secs] [Times: user=0.04 sys=0.08, real=0.04 secs] 
2020-10-26T21:48:27.315-0800: [GC (Allocation Failure) [PSYoungGen: 455006K->100338K(486912K)] 637374K->343673K(783872K), 0.0796518 secs] [Times: user=0.07 sys=0.17, real=0.08 secs] 
2020-10-26T21:48:27.395-0800: [Full GC (Ergonomics) [PSYoungGen: 100338K->0K(486912K)] [ParOldGen: 243335K->266120K(401920K)] 343673K->266120K(888832K), [Metaspace: 2720K->2720K(1056768K)], 0.0553723 secs] [Times: user=0.11 sys=0.03, real=0.06 secs] 
2020-10-26T21:48:27.526-0800: [GC (Allocation Failure) [PSYoungGen: 386560K->109535K(684544K)] 652680K->375655K(1086464K), 0.0575981 secs] [Times: user=0.05 sys=0.12, real=0.06 secs] 
执行结束!共生成对象次数:7400
Heap
 PSYoungGen      total 684544K, used 380518K [0x000000076ab00000, 0x000000079ed80000, 0x00000007c0000000)
  eden space 543232K, 49% used [0x000000076ab00000,0x000000077b3a1a58,0x000000078bd80000)
  from space 141312K, 77% used [0x0000000795280000,0x000000079bd77da8,0x000000079dc80000)
  to   space 152576K, 0% used [0x000000078bd80000,0x000000078bd80000,0x0000000795280000)
 ParOldGen       total 401920K, used 266120K [0x00000006c0000000, 0x00000006d8880000, 0x000000076ab00000)
  object space 401920K, 66% used [0x00000006c0000000,0x00000006d03e21f8,0x00000006d8880000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K


java -Xmx512m -Xms512m -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
发生了24次young gc，5次full gc
正在执行...
2020-10-26T21:54:07.883-0800: [GC (Allocation Failure) [PSYoungGen: 131584K->21485K(153088K)] 131584K->48979K(502784K), 0.0237879 secs] [Times: user=0.02 sys=0.05, real=0.02 secs] 
2020-10-26T21:54:07.934-0800: [GC (Allocation Failure) [PSYoungGen: 152860K->21494K(153088K)] 180354K->86880K(502784K), 0.0325624 secs] [Times: user=0.03 sys=0.07, real=0.03 secs] 
2020-10-26T21:54:07.994-0800: [GC (Allocation Failure) [PSYoungGen: 153023K->21503K(153088K)] 218409K->126796K(502784K), 0.0237328 secs] [Times: user=0.03 sys=0.05, real=0.02 secs] 
2020-10-26T21:54:08.043-0800: [GC (Allocation Failure) [PSYoungGen: 153087K->21501K(153088K)] 258380K->167519K(502784K), 0.0267377 secs] [Times: user=0.04 sys=0.05, real=0.03 secs] 
2020-10-26T21:54:08.095-0800: [GC (Allocation Failure) [PSYoungGen: 153085K->21491K(153088K)] 299103K->212511K(502784K), 0.0289470 secs] [Times: user=0.04 sys=0.06, real=0.03 secs] 
2020-10-26T21:54:08.148-0800: [GC (Allocation Failure) [PSYoungGen: 153075K->21500K(80384K)] 344095K->252383K(430080K), 0.0299578 secs] [Times: user=0.04 sys=0.05, real=0.03 secs] 
2020-10-26T21:54:08.191-0800: [GC (Allocation Failure) [PSYoungGen: 80339K->37398K(116736K)] 311222K->272044K(466432K), 0.0082040 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:54:08.214-0800: [GC (Allocation Failure) [PSYoungGen: 96059K->50060K(116736K)] 330705K->291570K(466432K), 0.0114676 secs] [Times: user=0.02 sys=0.01, real=0.01 secs] 
2020-10-26T21:54:08.236-0800: [GC (Allocation Failure) [PSYoungGen: 108563K->57845K(116736K)] 350072K->310834K(466432K), 0.0153989 secs] [Times: user=0.03 sys=0.01, real=0.02 secs] 
2020-10-26T21:54:08.266-0800: [GC (Allocation Failure) [PSYoungGen: 116162K->37629K(116736K)] 369151K->326713K(466432K), 0.0243733 secs] [Times: user=0.03 sys=0.03, real=0.03 secs] 
2020-10-26T21:54:08.301-0800: [GC (Allocation Failure) [PSYoungGen: 96509K->19196K(116736K)] 385593K->342102K(466432K), 0.0249268 secs] [Times: user=0.03 sys=0.05, real=0.02 secs] 
2020-10-26T21:54:08.326-0800: [Full GC (Ergonomics) [PSYoungGen: 19196K->0K(116736K)] [ParOldGen: 322906K->242541K(349696K)] 342102K->242541K(466432K), [Metaspace: 2719K->2719K(1056768K)], 0.0530997 secs] [Times: user=0.12 sys=0.01, real=0.06 secs] 
2020-10-26T21:54:08.391-0800: [GC (Allocation Failure) [PSYoungGen: 58804K->22593K(116736K)] 301346K->265134K(466432K), 0.0044642 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:54:08.410-0800: [GC (Allocation Failure) [PSYoungGen: 81473K->21508K(116736K)] 324014K->285705K(466432K), 0.0077507 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:54:08.430-0800: [GC (Allocation Failure) [PSYoungGen: 80043K->16886K(116736K)] 344240K->300955K(466432K), 0.0066102 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:54:08.451-0800: [GC (Allocation Failure) [PSYoungGen: 75552K->22494K(116736K)] 359621K->322455K(466432K), 0.0072214 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:54:08.458-0800: [Full GC (Ergonomics) [PSYoungGen: 22494K->0K(116736K)] [ParOldGen: 299961K->275180K(349696K)] 322455K->275180K(466432K), [Metaspace: 2720K->2720K(1056768K)], 0.0497987 secs] [Times: user=0.11 sys=0.00, real=0.05 secs] 
2020-10-26T21:54:08.519-0800: [GC (Allocation Failure) [PSYoungGen: 58880K->20934K(116736K)] 334060K->296114K(466432K), 0.0041497 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:54:08.533-0800: [GC (Allocation Failure) [PSYoungGen: 79814K->20428K(116736K)] 354994K->315540K(466432K), 0.0072982 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
2020-10-26T21:54:08.552-0800: [GC (Allocation Failure) [PSYoungGen: 79308K->20190K(116736K)] 374420K->335614K(466432K), 0.0070106 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:54:08.559-0800: [Full GC (Ergonomics) [PSYoungGen: 20190K->0K(116736K)] [ParOldGen: 315424K->289755K(349696K)] 335614K->289755K(466432K), [Metaspace: 2720K->2720K(1056768K)], 0.0533954 secs] [Times: user=0.12 sys=0.01, real=0.06 secs] 
2020-10-26T21:54:08.627-0800: [GC (Allocation Failure) [PSYoungGen: 58868K->21291K(116736K)] 348623K->311046K(466432K), 0.0044977 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:54:08.642-0800: [GC (Allocation Failure) [PSYoungGen: 80171K->24760K(116736K)] 369926K->333144K(466432K), 0.0081523 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
2020-10-26T21:54:08.663-0800: [GC (Allocation Failure) [PSYoungGen: 83551K->20221K(116736K)] 391935K->352082K(466432K), 0.0146092 secs] [Times: user=0.02 sys=0.01, real=0.01 secs] 
2020-10-26T21:54:08.678-0800: [Full GC (Ergonomics) [PSYoungGen: 20221K->0K(116736K)] [ParOldGen: 331861K->308770K(349696K)] 352082K->308770K(466432K), [Metaspace: 2720K->2720K(1056768K)], 0.0546468 secs] [Times: user=0.13 sys=0.01, real=0.06 secs] 
2020-10-26T21:54:08.743-0800: [GC (Allocation Failure) [PSYoungGen: 58880K->22281K(116736K)] 367650K->331052K(466432K), 0.0044970 secs] [Times: user=0.02 sys=0.00, real=0.00 secs] 
2020-10-26T21:54:08.757-0800: [GC (Allocation Failure) [PSYoungGen: 81161K->22557K(116736K)] 389932K->352127K(466432K), 0.0074038 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
2020-10-26T21:54:08.765-0800: [Full GC (Ergonomics) [PSYoungGen: 22557K->0K(116736K)] [ParOldGen: 329570K->318420K(349696K)] 352127K->318420K(466432K), [Metaspace: 2720K->2720K(1056768K)], 0.0550847 secs] [Times: user=0.14 sys=0.00, real=0.06 secs] 
执行结束!共生成对象次数:6814
Heap
 PSYoungGen      total 116736K, used 2677K [0x00000007b5580000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 58880K, 4% used [0x00000007b5580000,0x00000007b581d438,0x00000007b8f00000)
  from space 57856K, 0% used [0x00000007b8f00000,0x00000007b8f00000,0x00000007bc780000)
  to   space 57856K, 0% used [0x00000007bc780000,0x00000007bc780000,0x00000007c0000000)
 ParOldGen       total 349696K, used 318420K [0x00000007a0000000, 0x00000007b5580000, 0x00000007b5580000)
  object space 349696K, 91% used [0x00000007a0000000,0x00000007b36f50f8,0x00000007b5580000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K


java -Xmx1g -Xms1g -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
发生了11次young gc
正在执行...
2020-10-26T22:53:05.138-0800: [GC (Allocation Failure)  262144K->78732K(1005056K), 0.0385587 secs]
2020-10-26T22:53:05.231-0800: [GC (Allocation Failure)  340634K->154142K(1005056K), 0.0644635 secs]
2020-10-26T22:53:05.348-0800: [GC (Allocation Failure)  416286K->224363K(1005056K), 0.0484617 secs]
2020-10-26T22:53:05.447-0800: [GC (Allocation Failure)  486507K->296200K(1005056K), 0.0481622 secs]
2020-10-26T22:53:05.537-0800: [GC (Allocation Failure)  558344K->374246K(1005056K), 0.0482651 secs]
2020-10-26T22:53:05.631-0800: [GC (Allocation Failure)  636390K->441796K(859648K), 0.0443951 secs]
2020-10-26T22:53:05.699-0800: [GC (Allocation Failure)  558532K->479716K(932352K), 0.0168082 secs]
2020-10-26T22:53:05.736-0800: [GC (Allocation Failure)  596452K->515522K(932352K), 0.0240382 secs]
2020-10-26T22:53:05.781-0800: [GC (Allocation Failure)  632258K->546294K(932352K), 0.0315578 secs]
2020-10-26T22:53:05.833-0800: [GC (Allocation Failure)  662917K->576174K(932352K), 0.0476662 secs]
2020-10-26T22:53:05.905-0800: [GC (Allocation Failure)  692910K->609557K(932352K), 0.0769542 secs]


java -Xmx2g -Xms2g -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
发生了3次young gc
正在执行...

2020-10-26T22:54:52.708-0800: [GC (Allocation Failure)  524800K->144302K(2010112K), 0.0711714 secs]
2020-10-26T22:54:52.882-0800: [GC (Allocation Failure)  669102K->255988K(2010112K), 0.0958994 secs]
2020-10-26T22:54:53.066-0800: [GC (Allocation Failure)  780788K->372362K(2010112K), 0.2212687 secs]
执行结束!共生成对象次数:7202


java -Xmx4g -Xms4g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
发生2次young gc
正在执行...
2020-10-26T21:55:16.813-0800: [GC (Allocation Failure) [PSYoungGen: 1048576K->174588K(1223168K)] 1048576K->242606K(4019712K), 0.1185188 secs] [Times: user=0.10 sys=0.27, real=0.12 secs] 
2020-10-26T21:55:17.127-0800: [GC (Allocation Failure) [PSYoungGen: 1223164K->174590K(1223168K)] 1291182K->368252K(4019712K), 0.4572329 secs] [Times: user=0.12 sys=0.24, real=0.46 secs] 
执行结束!共生成对象次数:7871
Heap
 PSYoungGen      total 1223168K, used 216614K [0x000000076ab00000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 1048576K, 4% used [0x000000076ab00000,0x000000076d40a1a8,0x00000007aab00000)
  from space 174592K, 99% used [0x00000007b5580000,0x00000007bffff9d0,0x00000007c0000000)
  to   space 174592K, 0% used [0x00000007aab00000,0x00000007aab00000,0x00000007b5580000)
 ParOldGen       total 2796544K, used 193662K [0x00000006c0000000, 0x000000076ab00000, 0x000000076ab00000)
  object space 2796544K, 6% used [0x00000006c0000000,0x00000006cbd1f948,0x000000076ab00000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K


三、CMS GC

concurrent mode failure CMS GC的过程中同时业务线程将对象放入老年代，而此时老年代空间不足，或者在做Minor GC的时候，新生代Survivor空间放不下，需要放入老年代，而老年代也放不下而产生的


java -Xmx512m -Xms512m  -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T21:56:33.240-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.240-0800: [ParNew: 139776K->17466K(157248K), 0.0259404 secs] 139776K->47359K(506816K), 0.0260260 secs] [Times: user=0.02 sys=0.05, real=0.02 secs] 
2020-10-26T21:56:33.300-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.300-0800: [ParNew: 157242K->17472K(157248K), 0.0249568 secs] 187135K->87563K(506816K), 0.0250030 secs] [Times: user=0.04 sys=0.06, real=0.02 secs] 
2020-10-26T21:56:33.353-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.353-0800: [ParNew: 157248K->17472K(157248K), 0.0314448 secs] 227339K->126410K(506816K), 0.0314915 secs] [Times: user=0.09 sys=0.02, real=0.03 secs] 
2020-10-26T21:56:33.415-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.415-0800: [ParNew: 157248K->17472K(157248K), 0.0254566 secs] 266186K->166875K(506816K), 0.0255081 secs] [Times: user=0.06 sys=0.03, real=0.03 secs] 
2020-10-26T21:56:33.467-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.467-0800: [ParNew: 156881K->17470K(157248K), 0.0324151 secs] 306284K->205146K(506816K), 0.0324540 secs] [Times: user=0.08 sys=0.03, real=0.04 secs] 
2020-10-26T21:56:33.500-0800: [GC (CMS Initial Mark) [1 CMS-initial-mark: 187676K(349568K)] 205537K(506816K), 0.0014906 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.501-0800: [CMS-concurrent-mark-start]
2020-10-26T21:56:33.515-0800: [CMS-concurrent-mark: 0.014/0.014 secs] [Times: user=0.02 sys=0.00, real=0.01 secs] 
2020-10-26T21:56:33.515-0800: [CMS-concurrent-preclean-start]
2020-10-26T21:56:33.516-0800: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.516-0800: [CMS-concurrent-abortable-preclean-start]
2020-10-26T21:56:33.535-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.535-0800: [ParNew: 157246K->17472K(157248K), 0.0291895 secs] 344922K->248378K(506816K), 0.0292326 secs] [Times: user=0.06 sys=0.03, real=0.03 secs] 
2020-10-26T21:56:33.591-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.591-0800: [ParNew: 157248K->17472K(157248K), 0.0301376 secs] 388154K->292618K(506816K), 0.0301888 secs] [Times: user=0.07 sys=0.02, real=0.03 secs] 
2020-10-26T21:56:33.651-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.651-0800: [ParNew: 157248K->17472K(157248K), 0.0358217 secs] 432394K->337075K(506816K), 0.0358693 secs] [Times: user=0.08 sys=0.03, real=0.03 secs] 
2020-10-26T21:56:33.711-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.711-0800: [ParNew: 157248K->157248K(157248K), 0.0000212 secs]2020-10-26T21:56:33.711-0800: [CMS2020-10-26T21:56:33.711-0800: [CMS-concurrent-abortable-preclean: 0.005/0.195 secs] [Times: user=0.31 sys=0.08, real=0.20 secs] 
 (concurrent mode failure): 319603K->254334K(349568K), 0.0593579 secs] 476851K->254334K(506816K), [Metaspace: 2719K->2719K(1056768K)], 0.0594348 secs] [Times: user=0.06 sys=0.01, real=0.06 secs] 
2020-10-26T21:56:33.793-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.793-0800: [ParNew: 139776K->17470K(157248K), 0.0085580 secs] 394110K->294694K(506816K), 0.0086079 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
2020-10-26T21:56:33.801-0800: [GC (CMS Initial Mark) [1 CMS-initial-mark: 277223K(349568K)] 295482K(506816K), 0.0001325 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.802-0800: [CMS-concurrent-mark-start]
2020-10-26T21:56:33.804-0800: [CMS-concurrent-mark: 0.003/0.003 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.804-0800: [CMS-concurrent-preclean-start]
2020-10-26T21:56:33.805-0800: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.805-0800: [CMS-concurrent-abortable-preclean-start]
2020-10-26T21:56:33.833-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.833-0800: [ParNew: 157246K->17472K(157248K), 0.0117549 secs] 434470K->336594K(506816K), 0.0117972 secs] [Times: user=0.04 sys=0.00, real=0.01 secs] 
2020-10-26T21:56:33.869-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.869-0800: [ParNew: 157122K->157122K(157248K), 0.0000275 secs]2020-10-26T21:56:33.869-0800: [CMS2020-10-26T21:56:33.869-0800: [CMS-concurrent-abortable-preclean: 0.002/0.064 secs] [Times: user=0.09 sys=0.00, real=0.06 secs] 
 (concurrent mode failure): 319122K->289871K(349568K), 0.0629226 secs] 476244K->289871K(506816K), [Metaspace: 2720K->2720K(1056768K)], 0.0630116 secs] [Times: user=0.06 sys=0.00, real=0.07 secs] 
2020-10-26T21:56:33.956-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.956-0800: [ParNew: 139776K->17472K(157248K), 0.0090816 secs] 429647K->337066K(506816K), 0.0091317 secs] [Times: user=0.03 sys=0.00, real=0.01 secs] 
2020-10-26T21:56:33.965-0800: [GC (CMS Initial Mark) [1 CMS-initial-mark: 319594K(349568K)] 337104K(506816K), 0.0001719 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.965-0800: [CMS-concurrent-mark-start]
2020-10-26T21:56:33.970-0800: [CMS-concurrent-mark: 0.005/0.005 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:56:33.970-0800: [CMS-concurrent-preclean-start]
2020-10-26T21:56:33.971-0800: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.971-0800: [CMS-concurrent-abortable-preclean-start]
2020-10-26T21:56:33.971-0800: [CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.971-0800: [GC (CMS Final Remark) [YG occupancy: 45061 K (157248 K)]2020-10-26T21:56:33.971-0800: [Rescan (parallel) , 0.0007965 secs]2020-10-26T21:56:33.972-0800: [weak refs processing, 0.0000194 secs]2020-10-26T21:56:33.972-0800: [class unloading, 0.0003365 secs]2020-10-26T21:56:33.972-0800: [scrub symbol table, 0.0007405 secs]2020-10-26T21:56:33.973-0800: [scrub string table, 0.0001955 secs][1 CMS-remark: 319594K(349568K)] 364655K(506816K), 0.0021770 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.973-0800: [CMS-concurrent-sweep-start]
2020-10-26T21:56:33.974-0800: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.974-0800: [CMS-concurrent-reset-start]
2020-10-26T21:56:33.975-0800: [CMS-concurrent-reset: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:33.999-0800: [GC (Allocation Failure) 2020-10-26T21:56:33.999-0800: [ParNew: 157248K->17471K(157248K), 0.0206776 secs] 443992K->348631K(506816K), 0.0207315 secs] [Times: user=0.06 sys=0.01, real=0.02 secs] 
2020-10-26T21:56:34.020-0800: [GC (CMS Initial Mark) [1 CMS-initial-mark: 331159K(349568K)] 348764K(506816K), 0.0001522 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.020-0800: [CMS-concurrent-mark-start]
2020-10-26T21:56:34.023-0800: [CMS-concurrent-mark: 0.003/0.003 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.023-0800: [CMS-concurrent-preclean-start]
2020-10-26T21:56:34.024-0800: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.024-0800: [CMS-concurrent-abortable-preclean-start]
2020-10-26T21:56:34.024-0800: [CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.024-0800: [GC (CMS Final Remark) [YG occupancy: 37985 K (157248 K)]2020-10-26T21:56:34.024-0800: [Rescan (parallel) , 0.0016122 secs]2020-10-26T21:56:34.026-0800: [weak refs processing, 0.0000770 secs]2020-10-26T21:56:34.026-0800: [class unloading, 0.0009695 secs]2020-10-26T21:56:34.027-0800: [scrub symbol table, 0.0014807 secs]2020-10-26T21:56:34.029-0800: [scrub string table, 0.0004572 secs][1 CMS-remark: 331159K(349568K)] 369145K(506816K), 0.0048799 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.030-0800: [CMS-concurrent-sweep-start]
2020-10-26T21:56:34.031-0800: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.031-0800: [CMS-concurrent-reset-start]
2020-10-26T21:56:34.032-0800: [CMS-concurrent-reset: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.051-0800: [GC (Allocation Failure) 2020-10-26T21:56:34.051-0800: [ParNew: 157222K->157222K(157248K), 0.0000329 secs]2020-10-26T21:56:34.051-0800: [CMS: 297310K->314608K(349568K), 0.0660848 secs] 454532K->314608K(506816K), [Metaspace: 2720K->2720K(1056768K)], 0.0661900 secs] [Times: user=0.06 sys=0.00, real=0.06 secs] 
2020-10-26T21:56:34.117-0800: [GC (CMS Initial Mark) [1 CMS-initial-mark: 314608K(349568K)] 314752K(506816K), 0.0001879 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.117-0800: [CMS-concurrent-mark-start]
2020-10-26T21:56:34.120-0800: [CMS-concurrent-mark: 0.003/0.003 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
2020-10-26T21:56:34.120-0800: [CMS-concurrent-preclean-start]
2020-10-26T21:56:34.121-0800: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
2020-10-26T21:56:34.121-0800: [CMS-concurrent-abortable-preclean-start]
执行结束!共生成对象次数:7942
Heap
 par new generation   total 157248K, used 45854K [0x00000007a0000000, 0x00000007aaaa0000, 0x00000007aaaa0000)
  eden space 139776K,  32% used [0x00000007a0000000, 0x00000007a2cc7ae8, 0x00000007a8880000)
  from space 17472K,   0% used [0x00000007a8880000, 0x00000007a8880000, 0x00000007a9990000)
  to   space 17472K,   0% used [0x00000007a9990000, 0x00000007a9990000, 0x00000007aaaa0000)
 concurrent mark-sweep generation total 349568K, used 314608K [0x00000007aaaa0000, 0x00000007c0000000, 0x00000007c0000000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K


java -Xmx1g -Xms1g -XX:+UseConcMarkSweepGC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T22:56:13.702-0800: [GC (Allocation Failure)  272640K->84623K(1014528K), 0.0419697 secs]
2020-10-26T22:56:13.803-0800: [GC (Allocation Failure)  357263K->159459K(1014528K), 0.0528699 secs]
2020-10-26T22:56:13.908-0800: [GC (Allocation Failure)  432009K->240331K(1014528K), 0.0711073 secs]
2020-10-26T22:56:14.023-0800: [GC (Allocation Failure)  512971K->322072K(1014528K), 0.0682674 secs]
2020-10-26T22:56:14.136-0800: [GC (Allocation Failure)  594712K->401611K(1014528K), 0.0680006 secs]
2020-10-26T22:56:14.204-0800: [GC (CMS Initial Mark)  407401K(1014528K), 0.0002608 secs]
2020-10-26T22:56:14.250-0800: [GC (Allocation Failure)  674251K->481403K(1014528K), 0.0931968 secs]
2020-10-26T22:56:14.344-0800: [GC (CMS Final Remark)  481417K(1014528K), 0.0140857 secs]
2020-10-26T22:56:14.457-0800: [GC (Allocation Failure)  627495K->436673K(1014528K), 0.0312435 secs]
执行结束!共生成对象次数:7708


java -Xmx2g -Xms2g -XX:+UseConcMarkSweepGC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T22:56:55.124-0800: [GC (Allocation Failure)  272640K->80494K(2063104K), 0.0415783 secs]
2020-10-26T22:56:55.226-0800: [GC (Allocation Failure)  353134K->163518K(2063104K), 0.0551827 secs]
2020-10-26T22:56:55.333-0800: [GC (Allocation Failure)  436158K->244910K(2063104K), 0.0752069 secs]
2020-10-26T22:56:55.452-0800: [GC (Allocation Failure)  517550K->317191K(2063104K), 0.0657764 secs]
2020-10-26T22:56:55.561-0800: [GC (Allocation Failure)  589584K->393159K(2063104K), 0.0722941 secs]
2020-10-26T22:56:55.677-0800: [GC (Allocation Failure)  665799K->474698K(2063104K), 0.0743522 secs]
2020-10-26T22:56:55.803-0800: [GC (Allocation Failure)  747338K->554260K(2063104K), 0.0813567 secs]
执行结束!共生成对象次数:8319



java -Xmx4g -Xms4g  -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T22:13:21.757-0800: [GC (Allocation Failure) 2020-10-26T22:13:21.757-0800: [ParNew: 272640K->34048K(306688K), 0.0646372 secs] 272640K->82103K(4160256K), 0.0646945 secs] [Times: user=0.09 sys=0.07, real=0.07 secs] 
2020-10-26T22:13:21.889-0800: [GC (Allocation Failure) 2020-10-26T22:13:21.889-0800: [ParNew: 306688K->34048K(306688K), 0.0754434 secs] 354743K->162295K(4160256K), 0.0754862 secs] [Times: user=0.11 sys=0.10, real=0.08 secs] 
2020-10-26T22:13:22.012-0800: [GC (Allocation Failure) 2020-10-26T22:13:22.012-0800: [ParNew: 306688K->34048K(306688K), 0.0956454 secs] 434935K->240414K(4160256K), 0.0956916 secs] [Times: user=0.20 sys=0.04, real=0.09 secs] 
2020-10-26T22:13:22.154-0800: [GC (Allocation Failure) 2020-10-26T22:13:22.154-0800: [ParNew: 306688K->34048K(306688K), 0.1021065 secs] 513054K->316372K(4160256K), 0.1021560 secs] [Times: user=0.22 sys=0.05, real=0.10 secs] 
2020-10-26T22:13:22.301-0800: [GC (Allocation Failure) 2020-10-26T22:13:22.301-0800: [ParNew: 306688K->34048K(306688K), 0.0919961 secs] 589012K->391646K(4160256K), 0.0920586 secs] [Times: user=0.19 sys=0.04, real=0.09 secs] 
2020-10-26T22:13:22.437-0800: [GC (Allocation Failure) 2020-10-26T22:13:22.437-0800: [ParNew: 306688K->34048K(306688K), 0.0902226 secs] 664286K->467891K(4160256K), 0.0902834 secs] [Times: user=0.20 sys=0.04, real=0.09 secs] 
执行结束!共生成对象次数:6858
Heap
 par new generation   total 306688K, used 230750K [0x00000006c0000000, 0x00000006d4cc0000, 0x00000006d4cc0000)
  eden space 272640K,  72% used [0x00000006c0000000, 0x00000006cc0179a0, 0x00000006d0a40000)
  from space 34048K, 100% used [0x00000006d0a40000, 0x00000006d2b80000, 0x00000006d2b80000)
  to   space 34048K,   0% used [0x00000006d2b80000, 0x00000006d2b80000, 0x00000006d4cc0000)
 concurrent mark-sweep generation total 3853568K, used 433843K [0x00000006d4cc0000, 0x00000007c0000000, 0x00000007c0000000)
 Metaspace       used 2726K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 296K, capacity 386K, committed 512K, reserved 1048576K


4、G1 GC

java -Xmx512m -Xms512m  -XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T22:14:59.164-0800: [GC pause (G1 Evacuation Pause) (young) 32M->8417K(512M), 0.0083233 secs]
2020-10-26T22:14:59.183-0800: [GC pause (G1 Evacuation Pause) (young) 34M->17M(512M), 0.0036850 secs]
2020-10-26T22:14:59.197-0800: [GC pause (G1 Evacuation Pause) (young) 46M->25M(512M), 0.0039481 secs]
2020-10-26T22:14:59.224-0800: [GC pause (G1 Evacuation Pause) (young) 78M->43M(512M), 0.0112799 secs]
2020-10-26T22:14:59.250-0800: [GC pause (G1 Evacuation Pause) (young) 95M->58M(512M), 0.0068527 secs]
2020-10-26T22:14:59.285-0800: [GC pause (G1 Evacuation Pause) (young) 129M->85M(512M), 0.0098953 secs]
2020-10-26T22:14:59.319-0800: [GC pause (G1 Evacuation Pause) (young) 170M->108M(512M), 0.0144205 secs]
2020-10-26T22:14:59.473-0800: [GC pause (G1 Evacuation Pause) (young) 390M->184M(512M), 0.0312421 secs]
2020-10-26T22:14:59.535-0800: [GC pause (G1 Evacuation Pause) (young) 294M->212M(512M), 0.0097024 secs]
2020-10-26T22:14:59.548-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 225M->216M(512M), 0.0034591 secs]
2020-10-26T22:14:59.551-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.552-0800: [GC concurrent-root-region-scan-end, 0.0002723 secs]
2020-10-26T22:14:59.552-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.556-0800: [GC concurrent-mark-end, 0.0041124 secs]
2020-10-26T22:14:59.556-0800: [GC remark, 0.0013404 secs]
2020-10-26T22:14:59.558-0800: [GC cleanup 236M->235M(512M), 0.0005354 secs]
2020-10-26T22:14:59.558-0800: [GC concurrent-cleanup-start]
2020-10-26T22:14:59.558-0800: [GC concurrent-cleanup-end, 0.0000258 secs]
2020-10-26T22:14:59.605-0800: [GC pause (G1 Evacuation Pause) (young)-- 423M->326M(512M), 0.0065090 secs]
2020-10-26T22:14:59.613-0800: [GC pause (G1 Evacuation Pause) (mixed) 331M->310M(512M), 0.0058497 secs]
2020-10-26T22:14:59.619-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 311M->311M(512M), 0.0008470 secs]
2020-10-26T22:14:59.620-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.620-0800: [GC concurrent-root-region-scan-end, 0.0001672 secs]
2020-10-26T22:14:59.620-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.622-0800: [GC concurrent-mark-end, 0.0018782 secs]
2020-10-26T22:14:59.622-0800: [GC remark, 0.0014512 secs]
2020-10-26T22:14:59.624-0800: [GC cleanup 323M->323M(512M), 0.0005970 secs]
2020-10-26T22:14:59.646-0800: [GC pause (G1 Evacuation Pause) (young) 417M->341M(512M), 0.0054111 secs]
2020-10-26T22:14:59.655-0800: [GC pause (G1 Evacuation Pause) (mixed) 358M->300M(512M), 0.0044918 secs]
2020-10-26T22:14:59.668-0800: [GC pause (G1 Evacuation Pause) (mixed) 326M->274M(512M), 0.0051778 secs]
2020-10-26T22:14:59.679-0800: [GC pause (G1 Evacuation Pause) (mixed) 304M->280M(512M), 0.0031860 secs]
2020-10-26T22:14:59.683-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 281M->281M(512M), 0.0012954 secs]
2020-10-26T22:14:59.684-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.684-0800: [GC concurrent-root-region-scan-end, 0.0001291 secs]
2020-10-26T22:14:59.684-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.686-0800: [GC concurrent-mark-end, 0.0017445 secs]
2020-10-26T22:14:59.686-0800: [GC remark, 0.0049385 secs]
2020-10-26T22:14:59.692-0800: [GC cleanup 293M->293M(512M), 0.0007413 secs]
2020-10-26T22:14:59.718-0800: [GC pause (G1 Evacuation Pause) (young) 399M->315M(512M), 0.0064142 secs]
2020-10-26T22:14:59.727-0800: [GC pause (G1 Evacuation Pause) (mixed) 330M->295M(512M), 0.0082998 secs]
2020-10-26T22:14:59.736-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 297M->295M(512M), 0.0019414 secs]
2020-10-26T22:14:59.739-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.739-0800: [GC concurrent-root-region-scan-end, 0.0001649 secs]
2020-10-26T22:14:59.739-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.741-0800: [GC concurrent-mark-end, 0.0023161 secs]
2020-10-26T22:14:59.741-0800: [GC remark, 0.0025535 secs]
2020-10-26T22:14:59.744-0800: [GC cleanup 305M->305M(512M), 0.0018994 secs]
2020-10-26T22:14:59.767-0800: [GC pause (G1 Evacuation Pause) (young) 406M->327M(512M), 0.0051049 secs]
2020-10-26T22:14:59.775-0800: [GC pause (G1 Evacuation Pause) (mixed) 345M->308M(512M), 0.0077603 secs]
2020-10-26T22:14:59.785-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 316M->310M(512M), 0.0023373 secs]
2020-10-26T22:14:59.788-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.788-0800: [GC concurrent-root-region-scan-end, 0.0001560 secs]
2020-10-26T22:14:59.788-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.792-0800: [GC concurrent-mark-end, 0.0041287 secs]
2020-10-26T22:14:59.792-0800: [GC remark, 0.0018966 secs]
2020-10-26T22:14:59.795-0800: [GC cleanup 322M->322M(512M), 0.0014248 secs]
2020-10-26T22:14:59.816-0800: [GC pause (G1 Evacuation Pause) (young) 410M->341M(512M), 0.0056475 secs]
2020-10-26T22:14:59.825-0800: [GC pause (G1 Evacuation Pause) (mixed) 358M->322M(512M), 0.0080137 secs]
2020-10-26T22:14:59.835-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 328M->324M(512M), 0.0032323 secs]
2020-10-26T22:14:59.838-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.838-0800: [GC concurrent-root-region-scan-end, 0.0002320 secs]
2020-10-26T22:14:59.838-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.840-0800: [GC concurrent-mark-end, 0.0016749 secs]
2020-10-26T22:14:59.840-0800: [GC remark, 0.0017094 secs]
2020-10-26T22:14:59.842-0800: [GC cleanup 336M->336M(512M), 0.0007998 secs]
2020-10-26T22:14:59.861-0800: [GC pause (G1 Evacuation Pause) (young) 409M->348M(512M), 0.0053112 secs]
2020-10-26T22:14:59.870-0800: [GC pause (G1 Evacuation Pause) (mixed) 369M->330M(512M), 0.0090945 secs]
2020-10-26T22:14:59.879-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 331M->331M(512M), 0.0017885 secs]
2020-10-26T22:14:59.881-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.881-0800: [GC concurrent-root-region-scan-end, 0.0001714 secs]
2020-10-26T22:14:59.881-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.884-0800: [GC concurrent-mark-end, 0.0023090 secs]
2020-10-26T22:14:59.884-0800: [GC remark, 0.0025510 secs]
2020-10-26T22:14:59.886-0800: [GC cleanup 342M->342M(512M), 0.0005685 secs]
2020-10-26T22:14:59.903-0800: [GC pause (G1 Evacuation Pause) (young) 405M->354M(512M), 0.0043883 secs]
2020-10-26T22:14:59.912-0800: [GC pause (G1 Evacuation Pause) (mixed) 378M->344M(512M), 0.0056704 secs]
2020-10-26T22:14:59.918-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 344M->344M(512M), 0.0020077 secs]
2020-10-26T22:14:59.920-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.921-0800: [GC concurrent-root-region-scan-end, 0.0002017 secs]
2020-10-26T22:14:59.921-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.924-0800: [GC concurrent-mark-end, 0.0032931 secs]
2020-10-26T22:14:59.924-0800: [GC remark, 0.0032752 secs]
2020-10-26T22:14:59.930-0800: [GC cleanup 367M->367M(512M), 0.0006031 secs]
2020-10-26T22:14:59.938-0800: [GC pause (G1 Evacuation Pause) (young) 403M->361M(512M), 0.0041407 secs]
2020-10-26T22:14:59.951-0800: [GC pause (G1 Evacuation Pause) (mixed) 384M->351M(512M), 0.0062653 secs]
2020-10-26T22:14:59.958-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 354M->352M(512M), 0.0015741 secs]
2020-10-26T22:14:59.959-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.959-0800: [GC concurrent-root-region-scan-end, 0.0001476 secs]
2020-10-26T22:14:59.959-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.962-0800: [GC concurrent-mark-end, 0.0020352 secs]
2020-10-26T22:14:59.962-0800: [GC remark, 0.0020342 secs]
2020-10-26T22:14:59.964-0800: [GC cleanup 363M->363M(512M), 0.0008573 secs]
2020-10-26T22:14:59.974-0800: [GC pause (G1 Evacuation Pause) (young) 401M->367M(512M), 0.0039564 secs]
2020-10-26T22:14:59.987-0800: [GC pause (G1 Evacuation Pause) (mixed) 392M->356M(512M), 0.0072510 secs]
2020-10-26T22:14:59.994-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 358M->355M(512M), 0.0014691 secs]
2020-10-26T22:14:59.996-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:14:59.996-0800: [GC concurrent-root-region-scan-end, 0.0002544 secs]
2020-10-26T22:14:59.996-0800: [GC concurrent-mark-start]
2020-10-26T22:14:59.999-0800: [GC concurrent-mark-end, 0.0026754 secs]
2020-10-26T22:14:59.999-0800: [GC remark, 0.0019365 secs]
2020-10-26T22:15:00.001-0800: [GC cleanup 366M->366M(512M), 0.0006403 secs]
2020-10-26T22:15:00.011-0800: [GC pause (G1 Evacuation Pause) (young) 403M->370M(512M), 0.0073321 secs]
2020-10-26T22:15:00.022-0800: [GC pause (G1 Evacuation Pause) (mixed) 394M->356M(512M), 0.0084195 secs]
2020-10-26T22:15:00.033-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 359M->356M(512M), 0.0030308 secs]
2020-10-26T22:15:00.036-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:15:00.036-0800: [GC concurrent-root-region-scan-end, 0.0001580 secs]
2020-10-26T22:15:00.036-0800: [GC concurrent-mark-start]
2020-10-26T22:15:00.039-0800: [GC concurrent-mark-end, 0.0024473 secs]
2020-10-26T22:15:00.039-0800: [GC remark, 0.0018457 secs]
2020-10-26T22:15:00.041-0800: [GC cleanup 372M->372M(512M), 0.0004939 secs]
2020-10-26T22:15:00.048-0800: [GC pause (G1 Evacuation Pause) (young) 403M->369M(512M), 0.0046418 secs]
2020-10-26T22:15:00.060-0800: [GC pause (G1 Evacuation Pause) (mixed) 394M->362M(512M), 0.0080052 secs]
2020-10-26T22:15:00.069-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 366M->362M(512M), 0.0015960 secs]
2020-10-26T22:15:00.071-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:15:00.071-0800: [GC concurrent-root-region-scan-end, 0.0001726 secs]
2020-10-26T22:15:00.071-0800: [GC concurrent-mark-start]
2020-10-26T22:15:00.073-0800: [GC concurrent-mark-end, 0.0020039 secs]
2020-10-26T22:15:00.073-0800: [GC remark, 0.0020364 secs]
2020-10-26T22:15:00.076-0800: [GC cleanup 374M->374M(512M), 0.0005316 secs]
2020-10-26T22:15:00.086-0800: [GC pause (G1 Evacuation Pause) (young) 401M->372M(512M), 0.0039577 secs]
2020-10-26T22:15:00.095-0800: [GC pause (G1 Evacuation Pause) (mixed) 397M->365M(512M), 0.0094939 secs]
2020-10-26T22:15:00.105-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 366M->366M(512M), 0.0019244 secs]
2020-10-26T22:15:00.107-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:15:00.107-0800: [GC concurrent-root-region-scan-end, 0.0001360 secs]
2020-10-26T22:15:00.107-0800: [GC concurrent-mark-start]
2020-10-26T22:15:00.109-0800: [GC concurrent-mark-end, 0.0026381 secs]
2020-10-26T22:15:00.110-0800: [GC remark, 0.0026629 secs]
2020-10-26T22:15:00.113-0800: [GC cleanup 374M->374M(512M), 0.0006592 secs]
2020-10-26T22:15:00.118-0800: [GC pause (G1 Evacuation Pause) (young) 397M->375M(512M), 0.0029655 secs]
2020-10-26T22:15:00.127-0800: [GC pause (G1 Evacuation Pause) (mixed) 403M->368M(512M), 0.0111508  secs]




java -Xmx1g -Xms1g -XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T22:57:40.981-0800: [GC pause (G1 Evacuation Pause) (young) 65M->27M(1024M), 0.0090052 secs]
2020-10-26T22:57:41.010-0800: [GC pause (G1 Evacuation Pause) (young) 85M->51M(1024M), 0.0104256 secs]
2020-10-26T22:57:41.036-0800: [GC pause (G1 Evacuation Pause) (young) 108M->72M(1024M), 0.0115448 secs]
2020-10-26T22:57:41.060-0800: [GC pause (G1 Evacuation Pause) (young) 125M->94M(1024M), 0.0102679 secs]
2020-10-26T22:57:41.087-0800: [GC pause (G1 Evacuation Pause) (young) 158M->115M(1024M), 0.0150643 secs]
2020-10-26T22:57:41.141-0800: [GC pause (G1 Evacuation Pause) (young) 216M->150M(1024M), 0.0151940 secs]
2020-10-26T22:57:41.175-0800: [GC pause (G1 Evacuation Pause) (young) 243M->181M(1024M), 0.0150045 secs]
2020-10-26T22:57:41.236-0800: [GC pause (G1 Evacuation Pause) (young) 310M->223M(1024M), 0.0206040 secs]
2020-10-26T22:57:41.331-0800: [GC pause (G1 Evacuation Pause) (young) 415M->269M(1024M), 0.0211106 secs]
2020-10-26T22:57:41.390-0800: [GC pause (G1 Evacuation Pause) (young) 436M->313M(1024M), 0.0230703 secs]
2020-10-26T22:57:41.511-0800: [GC pause (G1 Evacuation Pause) (young) 592M->391M(1024M), 0.0334542 secs]
2020-10-26T22:57:41.572-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 522M->429M(1024M), 0.0157248 secs]
2020-10-26T22:57:41.587-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:57:41.588-0800: [GC concurrent-root-region-scan-end, 0.0002294 secs]
2020-10-26T22:57:41.588-0800: [GC concurrent-mark-start]
2020-10-26T22:57:41.594-0800: [GC concurrent-mark-end, 0.0060848 secs]
2020-10-26T22:57:41.594-0800: [GC remark, 0.0019939 secs]
2020-10-26T22:57:41.596-0800: [GC cleanup 456M->442M(1024M), 0.0010069 secs]
2020-10-26T22:57:41.597-0800: [GC concurrent-cleanup-start]
2020-10-26T22:57:41.597-0800: [GC concurrent-cleanup-end, 0.0000320 secs]
2020-10-26T22:57:41.772-0800: [GC pause (G1 Evacuation Pause) (young)-- 863M->663M(1024M), 0.0221709 secs]
2020-10-26T22:57:41.796-0800: [GC pause (G1 Evacuation Pause) (mixed) 668M->593M(1024M), 0.0157335 secs]
2020-10-26T22:57:41.823-0800: [GC pause (G1 Evacuation Pause) (mixed) 650M->600M(1024M), 0.0117986 secs]
2020-10-26T22:57:41.836-0800: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 607M->603M(1024M), 0.0023003 secs]
2020-10-26T22:57:41.839-0800: [GC concurrent-root-region-scan-start]
2020-10-26T22:57:41.839-0800: [GC concurrent-root-region-scan-end, 0.0001913 secs]
2020-10-26T22:57:41.839-0800: [GC concurrent-mark-start]
2020-10-26T22:57:41.843-0800: [GC concurrent-mark-end, 0.0037209 secs]
2020-10-26T22:57:41.843-0800: [GC remark, 0.0020438 secs]
2020-10-26T22:57:41.845-0800: [GC cleanup 619M->604M(1024M), 0.0009065 secs]
2020-10-26T22:57:41.846-0800: [GC concurrent-cleanup-start]
2020-10-26T22:57:41.846-0800: [GC concurrent-cleanup-end, 0.0000387 secs]
执行结束!共生成对象次数:7511




java -Xmx2g -Xms2g -XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T22:58:14.361-0800: [GC pause (G1 Evacuation Pause) (young) 126M->42M(2048M), 0.0140594 secs]
2020-10-26T22:58:14.408-0800: [GC pause (G1 Evacuation Pause) (young) 157M->82M(2048M), 0.0186210 secs]
2020-10-26T22:58:14.455-0800: [GC pause (G1 Evacuation Pause) (young) 191M->112M(2048M), 0.0179461 secs]
2020-10-26T22:58:14.502-0800: [GC pause (G1 Evacuation Pause) (young) 227M->150M(2048M), 0.0154333 secs]
2020-10-26T22:58:14.548-0800: [GC pause (G1 Evacuation Pause) (young) 265M->182M(2048M), 0.0190692 secs]
2020-10-26T22:58:14.592-0800: [GC pause (G1 Evacuation Pause) (young) 288M->212M(2048M), 0.0159615 secs]
2020-10-26T22:58:14.636-0800: [GC pause (G1 Evacuation Pause) (young) 321M->247M(2048M), 0.0186973 secs]
2020-10-26T22:58:14.709-0800: [GC pause (G1 Evacuation Pause) (young) 400M->287M(2048M), 0.0198033 secs]
2020-10-26T22:58:14.765-0800: [GC pause (G1 Evacuation Pause) (young) 446M->333M(2048M), 0.0252302 secs]
2020-10-26T22:58:14.843-0800: [GC pause (G1 Evacuation Pause) (young) 521M->381M(2048M), 0.0254740 secs]
2020-10-26T22:58:14.937-0800: [GC pause (G1 Evacuation Pause) (young) 616M->442M(2048M), 0.0314571 secs]
执行结束!共生成对象次数:7637



java -Xmx4g -Xms4g  -XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-26T22:16:34.457-0800: [GC pause (G1 Evacuation Pause) (young) 204M->61M(4096M), 0.0261875 secs]
2020-10-26T22:16:34.523-0800: [GC pause (G1 Evacuation Pause) (young) 239M->124M(4096M), 0.0304292 secs]
2020-10-26T22:16:34.594-0800: [GC pause (G1 Evacuation Pause) (young) 302M->180M(4096M), 0.0276191 secs]
2020-10-26T22:16:34.659-0800: [GC pause (G1 Evacuation Pause) (young) 358M->235M(4096M), 0.0304990 secs]
2020-10-26T22:16:34.726-0800: [GC pause (G1 Evacuation Pause) (young) 413M->292M(4096M), 0.0269250 secs]
2020-10-26T22:16:34.790-0800: [GC pause (G1 Evacuation Pause) (young) 470M->355M(4096M), 0.0303804 secs]
2020-10-26T22:16:34.855-0800: [GC pause (G1 Evacuation Pause) (young) 533M->414M(4096M), 0.0286377 secs]
2020-10-26T22:16:34.919-0800: [GC pause (G1 Evacuation Pause) (young) 592M->479M(4096M), 0.0298495 secs]
2020-10-26T22:16:34.986-0800: [GC pause (G1 Evacuation Pause) (young) 657M->529M(4096M), 0.0260526 secs]
2020-10-26T22:16:35.048-0800: [GC pause (G1 Evacuation Pause) (young) 707M->592M(4096M), 0.0278065 secs]
2020-10-26T22:16:35.131-0800: [GC pause (G1 Evacuation Pause) (young) 824M->667M(4096M), 0.0566068 secs]
2020-10-26T22:16:35.239-0800: [GC pause (G1 Evacuation Pause) (young) 911M->749M(4096M), 0.0681263 secs]
执行结束!共生成对象次数:8542




使用压测工具（wrk或sb），演练gateway-server-0.0.1-SNAPSHOT.jar示例
并行GC吞吐量较好，服务需要较好吞吐量可以选择并行GC 占用CPU较少
CMS GC延迟较低，服务需要较低延迟可以选择CMS GC 占用CPU较多

单核，堆内存较小选择串行GC

多核，堆内存较大（大于4G），选择G1 GC

Serial GC 

Xmx512m Xms512m    2829

Xmx1g Xms1g        2891

Xmx2g Xms2g        2970

Xmx4g Xms4g        3108

Parallel GC

Xmx512m Xms512m    3213

Xmx1g Xms1g        3354

Xmx2g Xms2g        3491

Xmx4g Xms4g        4397



ConcMarkSweep GC

Xmx512m Xms512m    2641

Xmx1g Xms1g        2970

Xmx2g Xms2g        2848

Xmx4g Xms4g        2813

G1 GC

Xmx512m Xms512m    2854

Xmx1g Xms1g        2938

Xmx2g Xms2g        3041

Xmx4g Xms4g        3058






