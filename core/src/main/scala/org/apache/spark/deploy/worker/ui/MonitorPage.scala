/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.deploy.worker.ui

import java.io.File
import java.net.URI
import javax.servlet.http.HttpServletRequest

import scala.xml.Node

import org.apache.spark.ui.{WebUIPage, UIUtils}
import org.apache.spark.util.Utils
import org.apache.spark.Logging
import org.apache.spark.util.logging.RollingFileAppender

private[ui] class MonitorPage(parent: WorkerWebUI) extends WebUIPage("monitorPage") with Logging {
  private val worker = parent.worker
  private val workDir = parent.workDir

  def renderMonitor(request: HttpServletRequest): String = {

    val appId = Option(request.getParameter("appId"))
    val executorId = Option(request.getParameter("executorId"))

    val logDir= (appId, executorId) match {
      case (Some(a), Some(e)) =>
        s"${workDir.getPath}/$a/$e/"
      case _ =>
        throw new Exception("Request must specify application identifiers")
      }
    "renderMonitor ret"
  }

  def render(request: HttpServletRequest): Seq[Node] = {

    val appId = Option(request.getParameter("appId"))
    val executorId = Option(request.getParameter("executorId"))

    val logDir= (appId, executorId) match {
      case (Some(a), Some(e)) =>
        s"${workDir.getPath}/$a/$e/"
      case _ =>
        throw new Exception("Request must specify application identifiers")
    }
    val filePath = "'" + logDir + "resourcelog.tsv" + "'"
    val linkToMaster = <p><a href={worker.activeMasterWebUiUrl}>Back to Master</a></p>

    val content =
      <html>
        <body>
          {linkToMaster}
          {UIUtils.customScriptAddOn}
          <div id="main" style="width: 960px;height:600px;"></div>
            <script type="text/javascript">
              myChart = echarts.init(document.getElementById('main'));
              init_monitor();
              draw({filePath})
            </script>
        </body>
      </html>
    UIUtils.basicSparkPage(content, " monitor page for " + appId.get + "/" + executorId.get)
  }
}
