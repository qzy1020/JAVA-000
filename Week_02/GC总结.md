一、使用GCLogAnalysis.java 演练串行/并行/CMS/G1
分别测试Xmx128m Xms128m；
Xmx512m Xms512m；
Xmx1g Xms1g；
Xmx2g Xms2g；
Xmx4g Xms4g；

1. 不管选取哪种GC，堆内存Xmx和Xms设置过小（128m），容易发生OutOfMemmoryErr内存溢出错误

2. 堆内存Xmx和Xms设置较小（512m），串行/并行/CMS会触发多次young gc和相对少量full gc，G1触发多次young gc和相对少量mixed gc，说明堆内存设置较小，young区创建对象速率快回收速率慢，以及young区部分对象到达次数晋升到老年代或者大对象直接晋升到老年代或者年轻代内存不够触发老年代创建对象，进一步触发老年代gc

3. CMS GC（设置512m堆内存）触发了concurrent mode failure，CMS GC的过程中将对象放入老年代，而此时老年代空间不足，或者在做Minor GC的时候，新生代Survivor空间放不下，需要放入老年代，而老年代也放不下而产生的，此时会触发full gc
   没有触发promotion failed（晋升失败）：当新生代发生垃圾回收，老年代有足够的空间  可以容纳晋升的对象，但是由于空闲空间的碎片化，导致晋升失败，此时会触发单线程且带压缩动作的 Full GC

4. 当堆内存设置加大时，串行/并行/CMS/G1垃圾回收效率都有所提升，具体哪种算法好，需要根据具体业务场景来选取合适的算法

5. 当单核cpu，堆内存较小时选择串行GC
当多核cpu，堆内存较大时，考虑吞吐量（GC并行处理，暂停时间总体较短，吞吐量较高），可选择并行GC
当多核cpu，堆内存较大时，考虑延迟低（GC并发处理，不影响用户线程，延迟较低），可选择CMS GC
当多核cpu，堆内存很大时（GC堆内存划分多个大小相等的独立区域（Region），能预测暂停时间和回收区域），可选择性能较好的G1 GC



二、使用压测工具（wrk或sb），演练gateway-server-0.0.1-SNAPSHOT.jar示例

* 根据压测数据并行GC吞吐量较好，串行和G1吞吐量差不多，CMS相对差些
并行GC吞吐量较好，服务需要较好吞吐量可以选择并行GC 占用CPU时间较少，暂停相对较短
CMS GC延迟较低，服务需要较低延迟可以选择CMS GC 占用CPU较多，并发处理，延迟较低

压测数据：
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

