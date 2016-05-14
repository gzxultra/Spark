package org.apache.spark.executor


 import java.util.concurrent.{Executors, ExecutorService}
 import java.lang.management.ManagementFactory
 import javax.xml.transform.Source
 import scala.io.Source
 import java.io._
 import java.util.Date
 import java.text.SimpleDateFormat
 import java.lang.Runtime
 class ExecutorMonitor(jobName : String, executorId : String, cores: Int) extends Thread {
   val workDir = "/usr/local/spark/work/"
   val path = workDir + jobName + "/" + executorId + "/"
   var flag = true
   var writer = new PrintWriter(new File(path + "resourcelog.tsv"))
   val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
   writer.write("")
   writer.close()

   val rt = Runtime.getRuntime()
   var exec_measure : String = null

   var execMem = rt.maxMemory().toDouble;
   if (execMem > 1024 * 1024 * 1024 * 1024){
    execMem = execMem / (1024 * 1024 * 1024 * 1024).toDouble
    exec_measure = "TB"
   } else if (execMem > 1024 * 1024 * 1024){
    execMem = execMem / (1024 * 1024 * 1024).toDouble
    exec_measure = "GB"
   } else if (execMem > 1024 * 1024) {
    execMem = execMem /  (1024 * 1024 ).toDouble
    exec_measure = "MB"
   } else if (execMem > 1024) {
      execMem = execMem /  1024.toDouble
      exec_measure = "KB"
   } else {
    execMem = execMem.toDouble
    exec_measure = "B"
   }

   def stopExecutorMonitor(){
     flag = false
     writer.close()
   }

   override def run(){
     var memRate : Double = 0.0
     var mem : Double = 0.0
     var VmHWM: Double = 0.0
     var VmRSS: Double = 0.0
     var cpuRate : Double = 0.0
     var HWM_measure : String = null
     var RSS_measure : String = null

     // from stackoverflow:
     // http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id
     val pid = ManagementFactory.getRuntimeMXBean().getName().split("@")(0)

     try{
        // make a snapshot
        var stat = scala.io.Source.fromFile("/proc/stat").getLines().next().split(" ")
        var pstat = scala.io.Source.fromFile("/proc/" + pid + "/stat").mkString.split(" ")

        // totalCpuTime = user + nice + system + idle + iowait + irq + softirq + stealstolen + guest
        var totalCpuTime1 : Double = 0
        for(i <- 2 to 10){
          totalCpuTime1 += stat(i).toInt
        }
        var idle1 : Double = 0.0 + stat(5).toInt

        // processCpuTime = utime + stime + cutime + cstime
        var processCpuTime1 : Double = 0
        for(i <- 13 to 16){
          processCpuTime1 += pstat(i).toInt
        }

      while (flag) {
        Thread.sleep(1000)
        stat = scala.io.Source.fromFile("/proc/stat").getLines().next().split(" ")
        pstat = scala.io.Source.fromFile("/proc/" + pid + "/stat").mkString.split(" ")

        var totalCpuTime2 : Double = 0
        for(i <- 2 to 10){
          totalCpuTime2 += stat(i).toInt
        }
        var idle2 : Double = 0.0 + stat(5).toInt

        var processCpuTime2 : Double = 0
        for(i <- 13 to 16){
          processCpuTime2 += pstat(i).toInt
        }

        cpuRate = 100.0 * ((processCpuTime2 - processCpuTime1) - (idle2 - idle1 )) / (totalCpuTime2 - totalCpuTime1)

        totalCpuTime1 = totalCpuTime2
        processCpuTime1 = processCpuTime2
        idle1 = idle2

        // http://blog.chinaunix.net/uid-22028680-id-3195672.html
        val status = scala.io.Source.fromFile("/proc/" + pid + "/status").getLines()
        for(line <- status){
            if( line.startsWith("VmRSS") ) {
                mem = line.split( ":" )(1).trim().split(" ")(0).toDouble
                VmRSS = line.split( ":" )(1).trim().split(" ")(0).toDouble

             if (VmRSS > 1024 * 1024 * 1024){
              VmRSS = VmRSS / (1024 * 1024 * 1024).toDouble
              RSS_measure = "TB"
             } else if (VmRSS > 1024 * 1024) {
              VmRSS = VmRSS / (1024 * 1024 ).toDouble
              RSS_measure = "GB"
             } else if (VmRSS > 1024) {
                VmRSS = VmRSS / 1024.toDouble
                RSS_measure = "MB"
             } else {
              VmRSS = VmRSS.toDouble
              RSS_measure = "KB"
             }
            }
            if( line.startsWith("VmHWM") ) {
                VmHWM = line.split( ":" )(1).trim().split(" ")(0).toDouble
             if (VmHWM > 1024 * 1024 * 1024) {
              VmHWM = VmHWM / (1024 * 1024 * 1024).toDouble
              HWM_measure = "TB"
             } else if (VmHWM > 1024 * 1024) {
              VmHWM = VmHWM /(1024 * 1024 ).toDouble
              HWM_measure = "GB"
             } else if (VmHWM > 1024) {
                VmHWM = VmHWM / 1024.toDouble
                HWM_measure = "MB"
             } else {
              VmHWM = VmHWM.toDouble
              HWM_measure = "KB"
             }
            }
        }
        memRate = 100.0 * mem * 1024 / execMem
        var fw = new FileWriter(path + "resourcelog.tsv", false)
        var writer = new PrintWriter(fw)
        // writer.write(df.format(new Date()) + "/" + f"$cpuRate%2.1f" + "/" + f"$memRate%2.1f" +"\n")
        // writer.printf("{\"date\": \"" + df.format(new Date()) + "\", \"cpu\": \"%s\",  \"mem\": \"%s\"}", f"$memRate%2.1f", f"$cpuRate%2.1f")
        writer.printf("{\"status\": " + "\"ok\"," + "\"content\": " + "{\"date\": \"" + df.format(new Date()) + "\", \"VmHWM\": \"%s %s\",  \"VmRSS\": \"%s %s\", \"execMem\": \"%s %s\"}}", f"$VmHWM%2.1f", HWM_measure, f"$VmRSS%2.1f", RSS_measure, f"$execMem%2.1f", exec_measure)
        writer.close()
        fw.close()
      }
    }
    catch{
      case e: Exception => e.printStackTrace()
    }
  }
}
