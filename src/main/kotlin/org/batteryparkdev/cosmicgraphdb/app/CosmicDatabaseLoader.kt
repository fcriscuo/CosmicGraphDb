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

    private val cosmicHGNCFile = resolveCosmicDataFile("CosmicHGNC.tsv")
    private val cosmicCompleteCNAFile = resolveCosmicDataFile("CosmicCompleteCNA.tsv")
    private val cosmicDiffMethylationFile = resolveCosmicDataFile("CosmicCompleteDifferentialMethylation.tsv")
    private val cosmicGeneExpressionFile = resolveCosmicDataFile("CosmicCompleteGeneExpression.tsv")
    private val cosmicGeneCensusFile = resolveCosmicDataFile("cancer_gene_census.csv")
    private val cosmicClassificationFile = resolveCosmicDataFile("classification.csv")
    private val cosmicSampleFile = resolveCosmicDataFile("CosmicSample.tsv")
    private val cosmicMutationExportCensusFile = resolveCosmicDataFile("CosmicMutantExportCensus.tsv")
    private val cosmicHallmarkFile = resolveCosmicDataFile("Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    private val cosmicBreakpointsFile = resolveCosmicDataFile("CancerBreakpointsExport.tsv")
    private val cosmicFusionFile = resolveCosmicDataFile("CosmicFusionExport.tsv")
    private val cosmicResistanceFile = resolveCosmicDataFile("CosmicResistanceMutation.tsv")
    private val cosmicStructFile = resolveCosmicDataFile("CosmicStructExport.tsv")
    private val cosmicNCVFile = resolveCosmicDataFile("CosmicNCV.tsv")


    private val nodeNameList = listOf<String>("CosmicHGNC",
        "CosmicHallmark", "CosmicTumor", "CosmicCodingMutation",
        "CosmicSample", "CosmicClassification", "CosmicGene", "CosmicType",
        "CosmicCompleteDNA", "CosmicGeneExpression", "CosmicDiffMethylation",
         "CosmicBreakpoint", "CosmicPatient", "CosmicNCV", "CosmicFusion",
        "CosmicResistanceMutation", "GeneMutationCollection", "SampleMutationCollection",
        "GenePublicationCollection", "SamplePublicationCollection","Publication")

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
            val task01 = asyncDefault { loadPubmedJob() }     // PubMed
            val task04 = asyncIO { loadClassificationJob() }  // CosmicClassification
            val task02 = asyncIO { loadGeneCensusJob() }      // CosmicGeneCensus
            // waiting for result of Job1 , Job2, & Job4
            val job4Result = task04.await()
            val job2Result = task02.await()
            // Job 5 waits for Job4
            val task05 = asyncIO { loadSampleJob(job4Result) }  // CosmicSample
            val job5Result = task05.await()
            // Job3  & job 3b wait for Job2 to complete  (CosmicGene)
            val task03 = asyncIO { loadHallmarkJob(job2Result) }  // CosmicHallmark
            val task3b = asyncIO { loadHGNCJob(job2Result) }  // Cosmic HGNC
            val job3Result = task03.await()
            val job3bResult = task3b.await()
            // Job6- Job 13 wait for Job5 (Cosmic Sample)
            val task06 = asyncIO { loadCodingMutations(job5Result) }  // Cosmic Coding Mutations
            val task07 = asyncIO { loadCompleteCNAJob(job5Result) }
            val task08 = asyncIO { loadDiffMethylationJob(job5Result) }
            val task09 = asyncIO { loadGeneExpressionJob(job5Result) }
            val task10 = asyncIO { loadBreakpointsJob(job5Result) }
            val task11 = asyncIO { loadCosmicStructJob(job5Result) }
            val task12 = asyncIO { loadCosmicResistanceMutationJob(job5Result) }
            val task13 = asyncIO { loadCosmicNCVJob( job5Result) }
            val task14 = asyncIO { loadCosmicFusionJob( job5Result) }
            // wait for last tier of jobs to complete
            onDone(task06.await() ,task01.await(), task07.await(), task08.await(), task09.await(), task10.await(),
            task11.await(), task12.await(), task13.await(), task14.await())
        }
    }

    private fun onDone(job6Result: String, job1Result: String, job7Result: String, job8Result: String, job9Result: String,
                       job10Result: String, job11Result: String, job12Result: String, job13Result:String, job14Result:String) {
        logger.atInfo().log("Executing onDone function")
        logger.atInfo().log(
            "task06 = $job6Result " +
            "task01 = $job1Result   task07 = $job7Result   " +
                    " task08 = $job8Result   task09 = $job9Result   task10 =$job10Result " +
                    "task11 = $job11Result   task12 = $job12Result   task13 =$job13Result " +
                    " task14 = $job14Result"
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

    private fun loadCodingMutations(job5Result: String): String {  //job 6
        logger.atInfo().log("6 - Starting CosmicCodingMutation loader")
       CosmicCodingMutationLoader.loadCosmicCodingMutationData(cosmicMutationExportCensusFile)
        return "MutantExport loaded"
    }

    private fun loadCompleteCNAJob(job3Result: String): String {  // job 7
        logger.atInfo().log("7 - Starting CompleteCNA loader")
        CosmicCompleteCNALoader.loadCosmicCompleteCNAData(cosmicCompleteCNAFile)
        return "CompleteCNA"
    }

    private fun loadDiffMethylationJob(job5Result: String): String {  // job 8
        logger.atInfo().log("8 - Starting DiffMethylation loader")
        CosmicDiffMethylationLoader.loadCosmicDiffMethylationData(cosmicDiffMethylationFile)
        return "Result of DiffMethylation loaded"
    }

    private fun loadGeneExpressionJob(job5Result: String): String { // job 9
        logger.atInfo().log("9 - Starting GeneExpression loader")
        CosmicCompleteGeneExpressionLoader.loadCosmicCompleteGeneExpressionData(cosmicGeneExpressionFile)
        return "Result of GeneExpression loaded"
    }

    private fun loadBreakpointsJob(job5Result: String): String {   // job 10
        logger.atInfo().log("10 - Starting Breakpoints loader")
        CosmicBreakpointLoader.loadCosmicBreakpointData(cosmicBreakpointsFile)
        return "Result of CosmicBreakpoints loaded"
    }

    private fun loadCosmicStructJob(job5Result: String): String {  // job 11
        CosmicStructLoader.loadCosmicStructFile(cosmicStructFile)
        return "Result of CosmicStruct data loaded"
    }

    private fun loadCosmicResistanceMutationJob(job5Result: String): String {  // job 12
        CosmicResistanceMutationLoader.loadCosmicResistanceMutationFile(cosmicResistanceFile)
        return "Result of CosmicResistanceMutation data loaded"
    }

    private fun loadCosmicNCVJob( job5Result: String): String {  // job 13
        CosmicNCVLoader.loadCosmicNCVFile(cosmicNCVFile)
        return "Result of CosmicNCV data loaded"
    }

    private fun loadCosmicFusionJob (Job5Result: String): String {
        CosmicFusionLoader.loadCosmicFusionData(cosmicFusionFile)
        return "Result of CosmicFusion Loader"
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

