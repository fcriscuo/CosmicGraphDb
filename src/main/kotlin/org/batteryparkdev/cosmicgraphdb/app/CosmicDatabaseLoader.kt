package org.batteryparkdev.cosmicgraphdb.app

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.*
import org.batteryparkdev.cosmicgraphdb.loader.*
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import org.batteryparkdev.property.service.ConfigurationPropertiesService.resolveCosmicSampleFileLocation
import org.batteryparkdev.publication.pubmed.loader.AsyncPubMedPublicationLoader
import kotlin.coroutines.CoroutineContext

/*
Primary COSMIC data loader
Loads COSMIC data files located in a directory either specified as a program argument or
defined in the datafiles.properties file (cosmic.data.directory)
n.b. This application deletes all existing COSMIC nodes and relationships from the
     Neo4j database
 */

class CosmicDatabaseLoader(val runMode: String = "sample") : CoroutineScope {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    private fun resolveSampleFile(fileProperty: String): String =
        resolveCosmicSampleFileLocation(fileProperty)

    private fun resolveCompleteFile(fileProperty: String): String =
        ConfigurationPropertiesService.resolveCosmicCompleteFileLocation(fileProperty)

    fun resolveCosmicDataFile(fileProperty: String): String =
        when (runMode.lowercase().equals("complete")) {
            true -> resolveCompleteFile(fileProperty)
            false -> resolveSampleFile(fileProperty)
        }

    private val cosmicHGNCFile = resolveCosmicDataFile("file.cosmic.hgnc")
    private val cosmicCompleteCNAFile = resolveCosmicDataFile("file.cosmic.complete.cna")
    private val cosmicDiffMethylationFile = resolveCosmicDataFile("file.cosmic.complete.differential.methylation")
    private val cosmicGeneExpressionFile = resolveCosmicDataFile("file.cosmic.complete.gene.expression")
    private val cosmicGeneCensusFile = resolveCosmicDataFile("file.cosmic.gene.census")
    private val cosmicClassificationFile = resolveCosmicDataFile("file.cosmic.classification")
    private val cosmicSampleFile = resolveCosmicDataFile("file.cosmic.sample")
    private val cosmicMutationExportCensusFile = resolveCosmicDataFile("file.cosmic.mutant.export.census")
    private val cosmicHallmarkFile = resolveCosmicDataFile("file.cosmic.gene.census.hallmarks.of.cancer")
    private val cosmicBreakpointsFile = resolveCosmicDataFile("file.cosmic.breakpoints.export")

    private val nodeNameList = listOf<String>("CosmicHGNC",
        "CosmicHallmark", "CosmicTumor", "CosmicMutation",
        "CosmicSample", "CosmicClassification", "CosmicGene", "CosmicType",
        "CosmicCompleteDNA", "CosmicGeneExpression", "CosmicDiffMethylation",
        "CosmicArticle", "CosmicBreakpoint", "Reference"
    )

    @OptIn(DelicateCoroutinesApi::class)
    val job = GlobalScope.launch() {
        delay(2000)
    }

    // creating local CoroutineContext
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    // extension function published on stackoverflow
    // source: https://stackoverflow.com/questions/53921470/how-to-run-two-jobs-in-parallel-but-wait-for-another-job-to-finish-using-kotlin
    fun <T> CoroutineScope.asyncIO(ioFun: () -> T) = async(Dispatchers.IO) { ioFun() }
    fun <T> CoroutineScope.asyncDefault(defaultFun: () -> T) = async(Dispatchers.Default) { defaultFun() }
    fun loadCosmicDatabase() = runBlocking {
        // load order is import for establishing parent to child relationships
        val stopwatch = Stopwatch.createStarted()
        deleteCosmicNodes()
        loadData()
        logger.atInfo().log("All currently supported COSMIC data have been loaded into Neo4j")
        logger.atInfo().log(
            "CosmicDatabase Loader elapsed time = ${stopwatch.elapsed(java.util.concurrent.TimeUnit.MINUTES)} " +
                    " minutes"
        )
    }

    /*
    Function to delete all Cosmic-related nodes and relationships
    prior to reloading the database
    n.b. PubMed-related nodes are NOT deleted
     */
    fun deleteCosmicNodes(): String {
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
            val task02 = asyncIO { loadGeneCensusJob() }
            // waiting for result of Job1 , Job2, & Job4
            val job4Result = task04.await()
            val job2Result = task02.await()
            // Job 5 waits for Job4
            val task05 = asyncIO { loadSampleJob(job4Result) }
            val job5Result = task05.await()
            // Job3 waits for Job2 to complete
            val task03 = asyncIO { loadHallmarkJob(job2Result) }
            val task3b = asyncIO { loadHGNCJob(job2Result) }
            // Job6 waits for Job5
            val task06 = asyncIO { loadMutantExportJob(job5Result) }
            // Job8 waits for Job5
            val task08 = asyncIO { loadDiffMethylationJob(job5Result) }
            val job3Result = task03.await()
            val job3bResult = task3b.await()
            val job6Result = task06.await()
            // Job7 and Job9 depend on Job3 & Job6
            val task07 = asyncIO { loadCompleteCNAJob(job2Result, job6Result) }
            val task09 = asyncIO { loadGeneExpressionJob(job2Result, job6Result) }
            // Job 10 depends on Job 6
            val task10 = asyncIO { loadBreakpointsJob(job6Result) }
            // wait for last tier of jobs to complete
            onDone(task01.await(), task07.await(), task08.await(), task09.await(), task10.await())
        }
    }

    private fun onDone(job1Result: String, job7Result: String, job8Result: String, job9Result: String, job10Result: String) {
        logger.atInfo().log("Executing onDone function")
        logger.atInfo().log(
            "task01 = $job1Result   task07 = $job7Result   " +
                    " task08 = $job8Result   task09 = $job9Result   task10 =$job10Result"
        )
        job.cancel()
    }

    /*
    Loading PubMed data into Neo4j is performed on a periodic basis concurrently with the other specialized
    loaders. The PubMed loader queries the database for placeholder nodes created by other loaders and
    queries NCBI to complete them.
     */
    private fun loadPubmedJob(): String {  // job 1
        logger.atInfo().log("1 - Starting PubMed loader")
        val taskDuration = 172_800_000L
        val timerInterval = 60_000L
        val scanTimer = AsyncPubMedPublicationLoader.scheduledPlaceHolderNodeScan(timerInterval)
        try {
            Thread.sleep(taskDuration)
        } finally {
            scanTimer.cancel();
        }
        return "PubMed loaded"
    }

    private fun loadGeneCensusJob(): String {  // job 2
        logger.atInfo().log("2 - Starting GeneCensus loader")
        CosmicGeneCensusLoader.loadCosmicGeneCensusData(cosmicGeneCensusFile)
        return "GeneCensus loaded"
    }

    private fun loadHallmarkJob(job2Result: String): String {  // job 3
        logger.atInfo().log("3 - Starting Hallmark loader")
        CosmicHallmarkLoader.processCosmicHallmarkData(cosmicHallmarkFile)
        return "Hallmark loaded"
    }

    private fun loadClassificationJob(): String {   // job 4
        logger.atInfo().log("4 - Starting Classification loader")
        CosmicClassificationLoader.loadCosmicClassificationData(cosmicClassificationFile)
        return "Classifications loaded"
    }

    private fun loadSampleJob(job4Result: String): String { // job 5
        logger.atInfo().log("5 - Starting Sample loader")
        CosmicSampleLoader.processCosmicSampleData(cosmicSampleFile)
        return "Result of sampleJob"
    }

    private fun loadMutantExportJob(job5Result: String): String {  //job 6
        logger.atInfo().log("6 - Starting MutantExport loader")
        CosmicMutantExportLoader.loadMutantExportFile(cosmicMutationExportCensusFile)
        return "MutantExport loaded"
    }

    private fun loadCompleteCNAJob(job3Result: String, job6Result: String): String {  // job 7
        logger.atInfo().log("7 - Starting CompleteCNA loader")
        CosmicCompleteCNALoader.loadCosmicCompleteCNAData(cosmicCompleteCNAFile)
        return "CompleteCNA"
    }

    private fun loadDiffMethylationJob(job3Result: String): String {  // job 8
        logger.atInfo().log("8 - Starting DiffMethylation loader")
        CosmicDiffMethylationLoader.loadCosmicDiffMethylationData(cosmicDiffMethylationFile)
        return "Result of DiffMethylation loaded"
    }

    private fun loadGeneExpressionJob(job3Result: String, job6Result: String): String { // job 9
        logger.atInfo().log("9 - Starting GeneExpression loader")
        CosmicCompleteGeneExpressionLoader.loadCosmicCompleteGeneExpressionData(cosmicGeneExpressionFile)
        return "Result of GeneExpression loaded"
    }

    private fun loadBreakpointsJob(job6Result: String): String {   // job 10
        logger.atInfo().log("10 - Starting Breakpoints loader")
        CosmicBreakpointLoader.loadCosmicBreakpointData(cosmicBreakpointsFile)
        return "Result of CosmicBreakpoints loaded"
    }

    private fun loadHGNCJob(job2Result: String): String {
        logger.atInfo().log("2b -Starting HGNC loader ")   // job 3b
        CosmicHGNCLoader.loadCosmicHGNCData(cosmicHGNCFile)
        return "Result of CosmicHGNC loading"
    }

}

fun main(args: Array<String>): Unit = runBlocking {
    val runMode =
        when (args.isNotEmpty()) {
            true -> args[0]
            false -> "sample"
        }
    println("WARNING: Invoking this application will delete all COSMIC data from the database")
    println("There will be a 20 second delay period to cancel this execution (CTRL-C) if this is not your intent")
    // Thread.sleep(20_000L)
    println("Cosmic data will now be loaded from the $runMode set of files")
    val loader = CosmicDatabaseLoader(runMode)
    loader.deleteCosmicNodes()
    loader.loadData()
    awaitCancellation()
}

