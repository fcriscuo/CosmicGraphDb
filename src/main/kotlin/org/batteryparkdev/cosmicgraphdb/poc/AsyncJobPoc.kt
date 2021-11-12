package org.batteryparkdev.cosmicgraphdb.poc

/*
source https://stackoverflow.com/questions/53921470/how-to-run-two-jobs-in-parallel-but-wait-for-another-job-to-finish-using-kotlin
 */

import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class AsyncJobPoc : CoroutineScope {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()
    // private var job: Job = Job()
    val job = GlobalScope.launch() {
        Thread.sleep(2000)
    }

    // creating local CoroutineContext
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // cancel the Job if it is no longer needed
    fun onClear() {
        job.cancel()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun doJob() {
        // launch coroutine
        GlobalScope.launch {
            // run Job1, Job2, and Job4 in parallel, asyncIO - is an extension function on CoroutineScope
            val d1 = asyncIO { pubmedJob() }
            val d4 = asyncIO { classificationJob() }
            val d2 = asyncIO { hallmarkJob() }

            // waiting for result of Job1 , Job2, & Job4

            val job4Result = d4.await()
            val job2Result = d2.await()

            // Job 5 waits for Job4
            val d5 = asyncIO { sampleJob(job4Result) }
            val job5Result = d5.await()

            // Job3 waits for Job2 to complete
            val d3 = asyncIO { geneCensusJob(job2Result) }
            // Job6 waits for Job5
            val d6 = asyncIO { mutantExportJob( job5Result) }
            // Job8 waits for Job5
            val d8 = asyncIO { diffMethylationJob(job5Result) }

            val job3Result = d3.await()
            val job6Result = d6.await()


            // Job7 and Job9 depend on Job3 & Job6

            val d7 = asyncIO{ completeCNAJob(job3Result, job6Result)}
            val d9 = asyncIO{ geneExpressionJob(job3Result, job6Result)}
            // wait for last tier of jobs to complete
            onDone(d1.await(),d7.await(), d8.await(), d9.await())
        }
    }

    private fun onDone(job1Result:String, job7Result: String, job8Result: String, job9Result: String) {
        logger.atInfo().log("Executing onDone function")
        logger.atInfo().log("d1 = $job1Result   d7 = $job7Result   d8 = $job8Result   d9 = $job9Result")
        onClear()
    }


     fun pubmedJob(): String {
        logger.atInfo().log("1 - Starting PubMed timer task")
         ScheduledTimerPoc().startTimerTask()
        return "PubMed loaded"
    }

     fun hallmarkJob(): String {
        logger.atInfo().log("2 - Starting Hallmark loader")
        Thread.sleep(60_000)
        return "Hallmark loaded"
    }

     fun geneCensusJob(job2Result: String): String {
        logger.atInfo().log("3 - Starting GeneCensus loader")
        Thread.sleep(45_000)
        return "GeneCensus loaded"
    }

     fun classificationJob(): String {
        logger.atInfo().log("4 - Starting Classification loader")
        Thread.sleep(20_000)
        return "Classifications loaded"
    }

     fun sampleJob( job4Result: String): String {
        logger.atInfo().log("5 - Starting Sample loader")
        Thread.sleep(80_000)
        return "Result of sampleJob"
    }


     fun mutantExportJob(job5Result: String): String {
        logger.atInfo().log("6 - Starting MutantExport loader")
        Thread.sleep(120_000)
        return "MutantExport loaded"
    }

     fun completeCNAJob(job3Result: String, job6Result: String): String {
        logger.atInfo().log("7 - Starting CompleteCNA loader")
        Thread.sleep(50_000)
        return "CompleteCNA"
    }

     fun diffMethylationJob(job3Result: String): String {
        logger.atInfo().log("8 - Starting DiffMethylation loader")
        Thread.sleep(40_000)
        return "Result of DiffMethylation loaded"
    }

     fun geneExpressionJob(job3Result: String, job6Result: String): String {
        logger.atInfo().log("9 - Starting GeneExpression loader")
        Thread.sleep(45_000)
        return "Result of GeneExpression loaded"
    }


    // extension function
    fun <T> CoroutineScope.asyncIO(ioFun: () -> T) = async(Dispatchers.IO) { ioFun() }
}

fun main() {
    runBlocking {
        AsyncJobPoc().doJob()
        awaitCancellation()
    }
}