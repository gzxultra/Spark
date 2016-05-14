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
 import org.json4s.JsonAST.{JNothing, JValue}

 import scala.io.Source
 import scala.xml.Node

 import org.apache.spark.ui.{WebUIPage, UIUtils}
 import org.apache.spark.util.Utils
 import org.apache.spark.Logging
 import org.apache.spark.util.logging.RollingFileAppender

 private[ui] class GetLog(parent: WorkerWebUI) extends WebUIPage("GetLog") with Logging {
  private val worker = parent.worker
  private val workDir = parent.workDir

  def renderGetLog(request: HttpServletRequest): String = {

    val appId = Option(request.getParameter("appId"))
    val executorId = Option(request.getParameter("executorId"))
    val logDir= (appId, executorId) match {
      case (Some(a), Some(e)) =>
      s"${workDir.getPath}/$a/$e/resourcelog.tsv"
      case _ =>
      throw new Exception("Request must specify application identifiers")
    }
    var ret : String = ""
    var lines : Iterator[String] = Source.fromFile(logDir).getLines
    lines.foreach{
      ret += _
    }
    ret
    /*
    var lines = Source.fromFile(logDir).getLines
    var tmp =lines.duplicate
    var size = tmp._1.size
    var fi : Iterator[String] = null
    if(  size > 60 ){
      fi = tmp._2.drop( size - 60 )
    }else{
      fi = tmp._2
    }
    fi.foreach{
      ret += _ + "#"
    }
   ret
   */
 }

 def render(request: HttpServletRequest): Seq[Node] = {
    logInfo("render called")
     val appId = Option(request.getParameter("appId"))
     val executorId = Option(request.getParameter("executorId"))
     val logDir= (appId, executorId) match {
      case (Some(a), Some(e)) =>
      s"${workDir.getPath}/$a/$e/resourcelog.tsv"
      case _ =>
      throw new Exception("Request must specify application identifiers")
  }
  /*
  var ret : String = ""
  var lines = Source.fromFile(logDir).getLines
  if( lines.size > 60 ){
    lines = lines.drop( lines.size - 60 )
  }
  lines.foreach{
    ret += _ + "#"
  }
  */
    var ret : String = ""
    var lines : Iterator[String] = Source.fromFile(logDir).getLines
    lines.foreach{
      ret += _
    }

  val linkToMaster = <p><a href={worker.activeMasterWebUiUrl}>Back to Master</a></p>
  val content=
  <html>
    <body>
    {linkToMaster}
    </body>
  </html>
  logInfo("render end")
  UIUtils.basicSparkPage(content, logDir)
}
}
