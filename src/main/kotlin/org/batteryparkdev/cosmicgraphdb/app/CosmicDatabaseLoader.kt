package org.batteryparkdev.cosmicgraphdb.app

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.*
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.neo4j.loader.*
import org.batteryparkdev.cosmicgraphdb.property.ApplicationPropertiesService
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService
import org.batteryparkdev.cosmicgraphdb.pubmed.loader.CosmicPubMedArticleLoader
import kotlin.coroutines.CoroutineContext
import kotlin.concurrent.timer

/*
Primary COSMIC data loader
Loads COSMIC data files located in a directory either specified as a program argument or
defined in the datafiles.properties file (cosmic.data.directory)
n.b. This application deletes all existing COSMIC nodes and relationships from the
     Neo4j database
 */

class CosmicDatabaseLoader(fileDirectory: String): CoroutineScope {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();
    private val cosmicHGNCFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.hgnc")
    private val cosmicCompleteCNAFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.complete.cna")
    private val cosmicDiffMethylationFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.complete.differential.methylation")
    private val cosmicGeneExpressionFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.complete.gene.expression")
    private val cosmicGeneCensusFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.gene.census")
    private val cosmicClassificationFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.classification")
    private val cosmicSampleFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.sample")
    private val cosmicMutationExportCensusFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.mutant.export.census")
    private val cosmicHallmarkFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.gene.census.hallmarks.of.cancer")
    private val cosmicBreakpointsFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.breakpoints.export")

    private val nodeNameList = listOf<String>(
        "CosmicHallmark", "CosmicTumor", "CosmicMutation",
        "CosmicSample", "CosmicClassification", "CosmicGene", "CosmicType",
        "CosmicCompleteDNA", "CosmicGeneExpression", "CosmicDiffMethylation",
        "CosmicArticle","CosmicBreakpoint","Reference"
    )
    @OptIn(DelicateCoroutinesApi::class)
    val job = GlobalScope.launch() {
        delay(2000)
    }

    // creating local CoroutineContext
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // extension function
    // source:source https://stackoverflow.com/questions/53921470/how-to-run-two-jobs-in-parallel-but-wait-for-another-job-to-finish-using-kotlin
    fun <T> CoroutineScope.asyncIO(ioFun: () -> T) = async(Dispatchers.IO) { ioFun() }
    fun <T> CoroutineScope.asyncDefault(defaultFun: () -> T) = async(Dispatchers.Default) { defaultFun() }
    fun loadCosmicDatabase() = runBlocking{
        // load order is import for establishing parent to child relationships
        val stopwatch = Stopwatch.createStarted()
        deleteCosmicNodes()
        loadData()
        logger.atInfo().log("All currently supported COSMIC data have been loaded into Neo4j")
        logger.atInfo().log("CosmicDatabase Loader elapsed time = ${stopwatch.elapsed(java.util.concurrent.TimeUnit.MINUTES)} " +
                " minutes")
    }

    /*
    Function to delete all Cosmic-related nodes and relationships
    prior to reloading the database
    n.b. PubMed-related nodes are NOT deleted
     */
    fun deleteCosmicNodes():String {
        nodeNameList.forEach { nodeName -> Neo4jUtils.detachAndDeleteNodesByName(nodeName) }
        return "CosmicDatabase nodes deleted"
    }

    /*
    Function to define the asynchronous workflow
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun loadData() {
        // launch coroutine
        GlobalScope.launch {
            // run Job1, Job2, and Job4 in parallel, asyncIO - is an extension function on CoroutineScope
            val task01 = asyncDefault { loadPubmedJob() }
            val task04 = asyncIO { loadClassificationJob() }
            val task02 = asyncIO {  loadGeneCensusJob() }
            // waiting for result of Job1 , Job2, & Job4
            val job4Result = task04.await()
            val job2Result = task02.await()
            // Job 5 waits for Job4
            val task05 = asyncIO { loadSampleJob(job4Result) }
            val job5Result = task05.await()
            // Job3 waits for Job2 to complete
            val task03 = asyncIO { loadHallmarkJob(job2Result) }
            // Job6 waits for Job5
            val task06 = asyncIO { loadMutantExportJob( job5Result) }
            // Job8 waits for Job5
            val task08 = asyncIO { loadDiffMethylationJob(job5Result) }
            val job3Result = task03.await()
            val job6Result = task06.await()
            // Job7 and Job9 depend on Job3 & Job6
            val task07 = asyncIO{ loadCompleteCNAJob(job2Result, job6Result)}
            val task09 = asyncIO{ loadGeneExpressionJob(job2Result, job6Result)}
            // Job 10 depends on Job 6
            val task10 =asyncIO { loadBreakpoints(job6Result) }
            // wait for last tier of jobs to complete
            onDone(task01.await(),task07.await(), task08.await(), task09.await(), task10.await())
        }
    }
    fun onDone(job1Result:String, job7Result: String, job8Result: String, job9Result: String, job10Result: String) {
        logger.atInfo().log("Executing onDone function")
        logger.atInfo().log("task01 = $job1Result   task07 = $job7Result   " +
                " task08 = $job8Result   task09 = $job9Result   task10 =$job10Result")
        job.cancel()
    }

  /*
  Loading PubMed data into Neo4j is performed on a periodic basis concurrently with the other specialized
  loaders. The PubMed loader queries the database for placeholder nodes created by other loaders and
  queries NCBI to complete them.
   */
    fun loadPubmedJob(): String {  // job 1
        logger.atInfo().log("1 - Starting PubMed loader")
      val taskDuration = 172_800_000L
      val timerInterval = 60_000L
      val scanTimer = CosmicPubMedArticleLoader.scheduledPlaceHolderNodeScan(timerInterval)
      try {
          Thread.sleep(taskDuration)
      } finally {
          scanTimer.cancel();
      }
        return "PubMed loaded"
    }

    fun loadGeneCensusJob(): String {  // job 2
        logger.atInfo().log("2 - Starting GeneCensus loader")
        CosmicGeneCensusLoader.loadCosmicGeneCensusData(cosmicGeneCensusFile)
        return "GeneCensus loaded"
    }

    fun loadHallmarkJob(job2Result: String): String {  // job 3
        logger.atInfo().log("3 - Starting Hallmark loader")
        CosmicHallmarkLoader.processCosmicHallmarkData(cosmicHallmarkFile)
        return "Hallmark loaded"
    }

    fun loadClassificationJob(): String {   // job 4
        logger.atInfo().log("4 - Starting Classification loader")
        CosmicClassificationLoader.loadCosmicClassificationData(cosmicClassificationFile)
        return "Classifications loaded"
    }

    fun loadSampleJob( job4Result: String): String { // job 5
        logger.atInfo().log("5 - Starting Sample loader")
        CosmicSampleLoader.processCosmicSampleData(cosmicSampleFile)
        return "Result of sampleJob"
    }

    fun loadMutantExportJob(job5Result: String): String {  //job 6
        logger.atInfo().log("6 - Starting MutantExport loader")
        CosmicMutantExportLoader.loadMutantExportFile(cosmicMutationExportCensusFile)
        return "MutantExport loaded"
    }

    fun loadCompleteCNAJob(job3Result: String, job6Result: String): String {  // job 7
        logger.atInfo().log("7 - Starting CompleteCNA loader")
        CosmicCompleteCNALoader.loadCosmicCompleteCNAData(cosmicCompleteCNAFile)
        return "CompleteCNA"
    }

    fun loadDiffMethylationJob(job3Result: String): String {  // job 8
        logger.atInfo().log("8 - Starting DiffMethylation loader")
        CosmicDiffMethylationLoader.loadCosmicDiffMethylationData(cosmicDiffMethylationFile)
        return "Result of DiffMethylation loaded"
    }

    fun loadGeneExpressionJob(job3Result: String, job6Result: String): String { // job 9
        logger.atInfo().log("9 - Starting GeneExpression loader")
        CosmicGeneExpressionLoader.loadCosmicCompleteGeneExpressionData(cosmicGeneExpressionFile)
        return "Result of GeneExpression loaded"
    }

    fun loadBreakpoints(job6Result:String): String {   // job 10
        logger.atInfo().log("10 - Starting Breakpoints loader")
        CosmicBreakpointLoader.processCosmicBreakpointData(cosmicBreakpointsFile)
        return "Result of CosmicBreakpoints loaded"
    }
}
    fun main(args: Array<String>) = runBlocking {
        val fileDirectory =
            when (args.isNotEmpty()) {
                true -> args[0]
                false -> DatafilePropertiesService.resolvePropertyAsString("cosmic.sample.data.directory")
            }
        println("WARNING: Invoking this application will delete all COSMIC data from the database")
        println("There will be a 20 second delay period to cancel this execution (CTRL-C) if this is not your intent")
       // Thread.sleep(20_000L)
        println("Cosmic data will now be loaded from: $fileDirectory")
        if (fileDirectory.isNotEmpty()) {
            val loader = CosmicDatabaseLoader(fileDirectory)
            loader.deleteCosmicNodes()
            loader.loadData()
            awaitCancellation()
        }
        println("CosmicDatabaseLoader has completed")
    }

