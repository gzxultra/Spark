// /*
//  * Licensed to the Apache Software Foundation (ASF) under one or more
//  * contributor license agreements.  See the NOTICE file distributed with
//  * this work for additional information regarding copyright ownership.
//  * The ASF licenses this file to You under the Apache License, Version 2.0
//  * (the "License"); you may not use this file except in compliance with
//  * the License.  You may obtain a copy of the License at
//  *
//  *    http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */

// package org.apache.spark.deploy.worker.ui

// import java.io.File
// import java.net.URI
// import javax.servlet.http.HttpServletRequest

// import scala.xml.Node

// import org.apache.spark.ui.{WebUIPage, UIUtils}

// import org.apache.spark.util.Utils
// import org.apache.spark.{Logging, SparkConf}
// import org.apache.spark.util.logging.RollingFileAppender
// import org.apache.spark.deploy.worker.ExecutorRunner
// import org.apache.spark.deploy.worker.Worker

// // import org.apache.spark.deploy.worker.worker

// private[ui] class KillExecutorPage(parent: WorkerWebUI) extends WebUIPage("KillExecutorPage") with Logging {
//   private val worker = parent.worker

//   def renderKillExecutor(request: HttpServletRequest, conf: SparkConf): String = {
//     val appId = Option(request.getParameter("appId"))
//     val executorId = Option(request.getParameter("executorId"))
//     var executorMemory = worker.conf.getOption("spark.executor.memory")
//     // worker.receive(killExecutors(Seq(executorId)))
//     "{\"status\":\"OK\",\"message\":\"%s\"}".format(executorMemory);
//   }

//   def render(request: HttpServletRequest): Seq[Node] = {
//     val appId = Option(request.getParameter("appId"))
//     val executorId = Option(request.getParameter("executorId"))
//     val content =
//       <html>
//         <body>
//           <div id="main" style="width: 1024px;height:768px;"></div>

//         </body>
//       </html>
//     UIUtils.basicSparkPage(content, " monitor page for " + appId.get + "/" + executorId.get)
//   }
// }
