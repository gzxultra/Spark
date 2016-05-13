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
import java.io._
import scala.xml.Node

import javax.servlet.http.HttpServletRequest
import org.json4s.JValue
import org.apache.spark.deploy.DeployMessages._
import org.apache.spark.deploy.master.{DriverState, Master}
import org.apache.spark.rpc._

import org.apache.spark.deploy.JsonProtocol
import org.apache.spark.deploy.DeployMessages.{RequestWorkerState, WorkerStateResponse}
import org.apache.spark.deploy.master.DriverState
import org.apache.spark.deploy.worker.{DriverRunner, ExecutorRunner, Worker}
import org.apache.spark.ui.{WebUIPage, UIUtils}
import org.apache.spark.util.Utils

private[ui] class WorkerPage(parent: WorkerWebUI) extends WebUIPage("") {
  private val workerEndpoint = parent.worker.self

  override def renderJson(request: HttpServletRequest): JValue = {
    val workerState = workerEndpoint.askWithRetry[WorkerStateResponse](RequestWorkerState)
    JsonProtocol.writeWorkerState(workerState)
  }

  def handleKillRequest(request: HttpServletRequest): Unit = {
    // val masterUrl = workerEndpoint.master.address.toSparkURL
    val worker = parent.worker

    val workerState = workerEndpoint.askWithRetry[WorkerStateResponse](RequestWorkerState)
    val masterUrl = workerState.masterUrl
    val appId = Option(request.getParameter("executor_appId"))
    val executorId = Option(request.getParameter("executor_execId"))

    // val killResult = workerEndpoint.askWithRetry[Boolean](KillExecutor(masterUrl, appId.get, executorId))
    val killResult = workerEndpoint.send(KillExecutor(masterUrl, appId.get, executorId.get.toInt))
    // val runningExecutors = workerState.executors

    // override def receive: PartialFunction[Any, Unit] = synchronized {
    // override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {

    // var executorMemory = 1024
    // var executorMemory = worker.conf.getOption("spark.executor.memory")
    // worker.receive(killExecutors(Seq(executorId)))
    var writer = new PrintWriter(new File("mylogfile.txt"))
    writer.write({killResult.toString})
    // writer.write("{\"status\":\"OK\",\"message\":\"%s\"}".format(killResult))
    writer.close()
  }

  def handleaddRequest(request: HttpServletRequest): Unit = {
    val cores = Option(request.getParameter("cores")).get.toInt
    val memory = Option(request.getParameter("memory")).get.split(" ")(0).toDouble.toInt * 1024
    // val appDesc = Option(request.getParameter("executor_appDesc")).get

    val worker = parent.worker

    var workerState = workerEndpoint.askWithRetry[WorkerStateResponse](RequestWorkerState)
    val masterUrl = workerState.masterUrl
    // val appId = Option(request.getParameter("executor_appId")).get
    val executorId = workerState.executors.length + 1

    val appDesc = workerState.executors(0).appDesc
    val appId = workerState.executors(0).appId

    // val appDesc = exec.askWithRetry.askWithRetry[ExecutorRunner]()

    val addResult = workerEndpoint.send(LaunchExecutor(masterUrl, appId, executorId, appDesc, cores, memory))


    // logInfo("Launching executor " + exec.executorId + " on worker " + worker.id)
    // worker.addExecutor(exec)
    // workerEndpoint.send(LaunchExecutor(masterUrl, appId, executorId, appDesc, cores, memory))
    // workerState = workerEndpoint.askWithRetry[WorkerStateResponse](RequestWorkerState)
    // val exec = workerState.executors(executorId).
    // exec.application.driver.send(
    //   ExecutorAdded(executorId, worker.id, worker.hostPort, cores, memory))


    var writer = new PrintWriter(new File("mylogfile.txt"))
    writer.write({addResult.toString})
    writer.close()
  }

  def render(request: HttpServletRequest): Seq[Node] = {
    val workerState = workerEndpoint.askWithRetry[WorkerStateResponse](RequestWorkerState)
    // val executorHeaders = Seq("ExecutorID", "Cores", "State", "Memory", "Job Details", "Logs")
    val executorHeaders = Seq("ExecutorID", "Cores", "State", "Memory", "Job Details", "Logs", "Resorce Monitor", "Operations")
    val runningExecutors = workerState.executors
    val runningExecutorTable =
      UIUtils.listingTable(executorHeaders, executorRow, runningExecutors)
    val finishedExecutors = workerState.finishedExecutors
    val finishedExecutorTable =
      UIUtils.listingTable(executorHeaders, executorRow, finishedExecutors)

    val driverHeaders = Seq("DriverID", "Main Class", "State", "Cores", "Memory", "Logs", "Notes")
    val runningDrivers = workerState.drivers.sortBy(_.driverId).reverse
    val runningDriverTable = UIUtils.listingTable(driverHeaders, driverRow, runningDrivers)
    val finishedDrivers = workerState.finishedDrivers.sortBy(_.driverId).reverse
    val finishedDriverTable = UIUtils.listingTable(driverHeaders, driverRow, finishedDrivers)

    // For now we only show driver information if the user has submitted drivers to the cluster.
    // This is until we integrate the notion of drivers and applications in the UI.
    val confirm =
      s"if (window.confirm('Are you sure you want to add an executor to worker ${workerState.workerId} ?')) " +
        "{ this.parentNode.submit(); return true; } else { return false; }"

    val content =
      <div class="row-fluid"> <!-- Worker Details -->
        <div class="span12">
          <ul class="unstyled">
            <li><strong>ID:</strong> {workerState.workerId}</li>
            <li><strong>
              Master URL:</strong> {workerState.masterUrl}
            </li>
            <li><strong>Cores:</strong> {workerState.cores} ({workerState.coresUsed} Used)</li>
            <li><strong>Memory:</strong> {Utils.megabytesToString(workerState.memory)}
              ({Utils.megabytesToString(workerState.memoryUsed)} Used)</li>
          </ul>
          <p><a href={workerState.masterWebUiUrl}>Back to Master</a></p>
        </div>
      </div>
      <div class="row-fluid"> <!-- Executors and Drivers -->
        <form action="worker/add/" method="POST" style="display:inline">
          <input type="hidden" id="cores" name="cores" value={workerState.cores.toString} />
          <input type="hidden" id="memory" name="memory" value={Utils.megabytesToString(workerState.memory)} />
          <input type="hidden" id="worker_name" name="worker_name" value={workerState.workerId.toString} />
          {
          if (runningExecutors.nonEmpty) {
              <button href="#" onclick={confirm} class="btn btn-primary add"> Add new executor! </button>
            }
            else {
              <button href="#" onclick={confirm} class="btn btn-primary add" disabled="disabled"> Add new executor! </button>
            }
          }
        </form>
        <div class="span12">
          <h4> Running Executors ({runningExecutors.size}) </h4>
          {runningExecutorTable}
          {
            if (runningDrivers.nonEmpty) {
              <h4> Running Drivers ({runningDrivers.size}) </h4> ++
              runningDriverTable
            }
          }
          {
            if (finishedExecutors.nonEmpty) {
              <h4>Finished Executors ({finishedExecutors.size}) </h4> ++
              finishedExecutorTable
            }
          }
          {
            if (finishedDrivers.nonEmpty) {
              <h4> Finished Drivers ({finishedDrivers.size}) </h4> ++
              finishedDriverTable
            }
          }
        </div>
      </div>
    UIUtils.basicSparkPage(content, "Spark Worker at %s:%s".format(
      workerState.host, workerState.port))
  }

  def executorRow(executor: ExecutorRunner): Seq[Node] = {
    val workerState = workerEndpoint.askWithRetry[WorkerStateResponse](RequestWorkerState)
    val runningExecutors = workerState.executors
    val confirm =
      s"if (window.confirm('Are you sure you want to kill executor ${executor.execId} ?')) " +
        "{ this.parentNode.submit(); return true; } else { return false; }"
    <tr>
      <td>{executor.execId}</td>
      <td>{executor.cores}</td>
      <td>{executor.state}</td>
      <td sorttable_customkey={executor.memory.toString}>
        {Utils.megabytesToString(executor.memory)}
      </td>
      <td>
        <ul class="unstyled">
          <li><strong>ID:</strong> {executor.appId}</li>
          <li><strong>Name:</strong> {executor.appDesc.name}</li>
          <li><strong>User:</strong> {executor.appDesc.user}</li>
        </ul>
      </td>
      <td>
     <a href={"logPage?appId=%s&executorId=%s&logType=stdout"
        .format(executor.appId, executor.execId)}>stdout</a>
     <a href={"logPage?appId=%s&executorId=%s&logType=stderr"
        .format(executor.appId, executor.execId)}>stderr</a>
      </td>
      <td>
        <a href={"monitorPage?appId=%s&executorId=%s"
          .format(executor.appId, executor.execId)}>monitor</a>
      </td>
      <td>
        <form action="worker/kill/" method="POST" style="display:inline">
          <input type="hidden" id="executor_appId" name="executor_appId" value={executor.appId.toString} />
          <input type="hidden" id="executor_execId" name="executor_execId" value={executor.execId.toString} />
          <input type="hidden" id="executor_appDesc" name="executor_appDesc" value={executor.appDesc.toString} />
        {
          if (runningExecutors.nonEmpty) {
            <button href="#" onclick={confirm} class="btn btn-danger kill"> kill </button>
          }
          else {
            <button href="#" onclick={confirm} class="btn btn-danger kill" disabled="disabled"> kill </button>
          }
        }
        </form>
      </td>
    </tr>
}

  def driverRow(driver: DriverRunner): Seq[Node] = {
    <tr>
      <td>{driver.driverId}</td>
      <td>{driver.driverDesc.command.arguments(2)}</td>
      <td>{driver.finalState.getOrElse(DriverState.RUNNING)}</td>
      <td sorttable_customkey={driver.driverDesc.cores.toString}>
        {driver.driverDesc.cores.toString}
      </td>
      <td sorttable_customkey={driver.driverDesc.mem.toString}>
        {Utils.megabytesToString(driver.driverDesc.mem)}
      </td>
      <td>
        <a href={s"logPage?driverId=${driver.driverId}&logType=stdout"}>stdout</a>
        <a href={s"logPage?driverId=${driver.driverId}&logType=stderr"}>stderr</a>
      </td>
      <td>
        {driver.finalException.getOrElse("")}
      </td>
    </tr>
  }
}
