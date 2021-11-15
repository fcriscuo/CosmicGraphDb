package org.batteryparkdev.cosmicgraphdb.poc

import com.google.common.flogger.FluentLogger
import java.util.*
import kotlin.concurrent.fixedRateTimer

class ScheduledTimerPoc {

    private val logger: FluentLogger = FluentLogger.forEnclosingClass();
    /*
    pubmed.timer.interval=10_000
pubmed.task.duration=172_800_000
     */

    fun startTimerTask() {
        val timer = scheduledPlaceHolderNodeScan()
        logger.atInfo().log("Timer started ...")
        Thread.sleep(172_800_000L)
        timer.cancel()
        logger.atInfo().log("Timer cancelled")
    }

    fun scheduledPlaceHolderNodeScan(interval: Long = 10_000): Timer {
        val fixedRateTimer = fixedRateTimer(name = "scan-timer",
            initialDelay = 5_000, period = interval) {
            processPlaceholderNodes()
        }
        return fixedRateTimer
    }

    private fun processPlaceholderNodes() {
        logger.atInfo().log("Timer invoked processPlaceholderNodes functions")
        Thread.sleep(2_000L)
    }

}

fun main(){
    ScheduledTimerPoc().startTimerTask()

}