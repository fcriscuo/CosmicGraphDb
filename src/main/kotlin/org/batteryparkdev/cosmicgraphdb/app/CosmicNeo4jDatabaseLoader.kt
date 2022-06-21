package org.batteryparkdev.cosmicgraphdb.app

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.*
import org.batteryparkdev.cosmicgraphdb.loader.*
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.publication.pubmed.loader.AsyncPubMedPublicationLoader
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/*
Primary COSMIC data loader
Loads COSMIC data files located in a directory either specified as a program argument or
defined in the datafiles.properties file (cosmic.data.directory)
n.b. This application deletes all existing COSMIC nodes and relationships from the
     Neo4j database
 */

class CosmicNeo4jDatabaseLoader(val runmode: String = "sample") : CoroutineScope {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

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
        println("All currently supported COSMIC data have been loaded into Neo4j")
        println(
            "CosmicDatabase Loader elapsed time = ${stopwatch.elapsed(java.util.concurrent.TimeUnit.MINUTES)} " +
                    " minutes"
        )
    }

    /*
    Function to delete all Cosmic-related nodes and relationships
    prior to reloading the database
     */
    fun deleteCosmicNodes(): String {
        CosmicFilenameService.nodeNameList.forEach { nodeName -> Neo4jUtils.detachAndDeleteNodesByName(nodeName) }
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
            // Job3  & job 3b wait for Job2 to complete  (CosmicGene)
            val task03 = asyncIO { loadHallmarkJob(job2Result) }  // CosmicHallmark
            val task3b = asyncIO { loadHGNCJob(job2Result) }  // Cosmic HGNC
            // Job 5 waits for Job4
            val task05 = asyncIO { loadSampleJob(job4Result) }  // CosmicSample
            val job5Result = task05.await()
            // Job 6- Job 14 wait for Job5 (Cosmic Sample)
            val task06 = asyncIO { loadCodingMutations(job5Result) }  // Cosmic Coding Mutations
            val task07 = asyncIO { loadCompleteCNAJob(job5Result) }
            val task08 = asyncIO { loadDiffMethylationJob(job5Result) }
            val task09 = asyncIO { loadGeneExpressionJob(job5Result) }
            val task10 = asyncIO { loadBreakpointsJob(job5Result) }
            val task11 = asyncIO { loadCosmicStructJob(job5Result) }
            val task12 = asyncIO { loadCosmicResistanceMutationsJob(job5Result) }
            val task13 = asyncIO { loadCosmicNCVJob(job5Result) }
            val task14 = asyncIO { loadCosmicFusionJob(job5Result) }
            // wait for last tier of jobs to complete
            onDone(
                task06.await(), task03.await(), task3b.await(),
                 task01.await(),
                task07.await(), task08.await(), task09.await(), task10.await(),
                task11.await(), task12.await(), task13.await(), task14.await()
            )
        }
    }

    private fun onDone(
        job6Result: String, job3Result: String, job3bResult: String,
         job1Result: String,
        job7Result: String, job8Result: String, job9Result: String,
        job10Result: String, job11Result: String, job12Result: String, job13Result: String, job14Result: String
    ) {
        println("Executing onDone function")
        println(
            "task06 = $job6Result " +
                      "task01 = $job1Result  " +
                    " task07 = $job7Result   " +
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
        println("1 - Starting PubMed loader")
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
        CosmicModelLoader(CosmicFilenameService.cosmicGeneCensusFile, runmode).also {
            println("Starting GeneCensus loader")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicGeneCensus data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "GeneCensus data loaded"
    }

    /*
    Private function to load the COSMIC hallmark data.
    This file has a UTF-16 encoding and requires a specialized loader
     */
    private fun loadHallmarkJob(job2Result: String): String {  // job 3
        val stopwatch = Stopwatch.createStarted()
        val filenameRunmodePair = Pair("Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv","complete")
        val hallmarkFile = CosmicFilenameService.resolveCosmicDataFile(filenameRunmodePair)
        println("Loading COSMIC Hallmark data from file: $hallmarkFile")
        CosmicHallmarkLoader.processCosmicHallmarkFile(hallmarkFile)
        println("CosmicHallmark data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        return "Hallmark data loaded"
    }

    private fun loadClassificationJob(): String {   // job 4
        CosmicModelLoader(CosmicFilenameService.cosmicClassificationFile, runmode).also {
            println("Starting Classification loader")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicClassification data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "Classifications data loaded"
    }

    private fun loadSampleJob(job4Result: String): String { // job 5
        CosmicModelLoader(CosmicFilenameService.cosmicSampleFile, runmode).also {
            println("Starting Sample loader")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicSample data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "Sample data loaded"
    }

    private fun loadCodingMutations(job5Result: String): String {  //job 6
        CosmicModelLoader(CosmicFilenameService.cosmicMutationExportCensusFile, runmode).also {
            println("Starting CosmicCodingMutation loader")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicCodingMutation data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "MutantExportCensus data loaded"
    }

    private fun loadCompleteCNAJob(job3Result: String): String {  // job 7
        CosmicModelLoader(CosmicFilenameService.cosmicCompleteCNAFile, runmode).also {
            println("Starting CompleteCNA loader")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicCompleteCNA data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "CompleteCNA data loaded"
    }

    private fun loadDiffMethylationJob(job5Result: String): String {  // job 8

        CosmicModelLoader(CosmicFilenameService.cosmicDiffMethylationFile, runmode).also {
            println("Starting DiffMethylation loader")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicDiffMethylation data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "DiffMethylation data loaded"
    }

    private fun loadGeneExpressionJob(job5Result: String): String { // job 9
        CosmicModelLoader(CosmicFilenameService.cosmicGeneExpressionFile, runmode).also {
            println("Starting GeneExpression loader")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicGeneExpression data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "GeneExpression data loaded"
    }

    private fun loadBreakpointsJob(job5Result: String): String {   // job 10
        CosmicModelLoader(CosmicFilenameService.cosmicBreakpointsFile, runmode).also {
            println("Starting Breakpoints loader")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicBreakpoints data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "CosmicBreakpoints data loaded"
    }

    private fun loadCosmicStructJob(job5Result: String): String {  // job 11

        CosmicModelLoader(CosmicFilenameService.cosmicStructFile, runmode).also {
            println("Starting Struct variant data loader ")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicStruct data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "CosmicStruct data loaded"
    }

    private fun loadCosmicResistanceMutationsJob(job5Result: String): String {  // job 12
        CosmicModelLoader(CosmicFilenameService.cosmicResistanceFile, runmode).also {
            println("Starting Drug Resistance data loader ")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicSResistanceMutations data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "Result of CosmicResistanceMutation data loaded"
    }

    private fun loadCosmicNCVJob(job5Result: String): String {  // job 13
        println("Starting NCV data loader ")
        CosmicModelLoader(CosmicFilenameService.cosmicNCVFile, runmode).also {
            println("Starting NCV data loader ")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicNCV data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "CosmicNCV data loaded"
    }

    private fun loadCosmicFusionJob(Job5Result: String): String {
        CosmicModelLoader(CosmicFilenameService.cosmicFusionFile, runmode).also {
            println("Starting Fusion data loader ")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicFusion data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "CosmicFusion data loaded"
    }

    private fun loadHGNCJob(job2Result: String): String {
        CosmicModelLoader(CosmicFilenameService.cosmicHGNCFile, runmode).also {
            println("3b -Starting HGNC loader ")
            val stopwatch = Stopwatch.createStarted()
            it.loadCosmicFile()
            println("CosmicHGNC data loading required ${stopwatch.elapsed(TimeUnit.MINUTES)} minutes")
        }
        return "CosmicHGNC data loaded"
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
    Thread.sleep(20_000L)
    println("Cosmic data will now be loaded from the $runMode set of files")
    val loader = CosmicNeo4jDatabaseLoader(runMode)
    loader.deleteCosmicNodes()
    loader.loadData()
    awaitCancellation()
}

