package org.batteryparkdev.pubmed.loader

import arrow.core.Either
import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService
import org.batteryparkdev.pubmed.dao.PubMedArticleDao
import org.batteryparkdev.pubmed.model.PubMedEntry
import org.batteryparkdev.pubmed.model.PubMedIdentifier
import org.batteryparkdev.pubmed.service.PubMedRetrievalService
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


/*
Responsible for completing PubMedArticle nodes by accessing
PubMed data from NCBI. Creates new empty (i.e. placeholder)
PubMedArticle nodes for PubMed articles referenced in by primary
PubMed articles.
 */
object CosmicPubMedArticleLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()


    /*
    Private function to query the Neo4j database for empty nodes
    and mapping their PubMed Ids to a channel of PubMedIdentifier objects
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.getPlacholders() =
        produce<PubMedIdentifier> {
            Neo4jUtils.resolvePlaceholderPubMedArticleNodes()
                .forEach { send(it) }
            delay(10)
        }

    /*
    Private function to retrieve PubMed data from NCBI and
    map it to a PubMedEntry model object
    The delay period is set to accommodate max NCBI request rate
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.retrievePubMedData(identifiers: ReceiveChannel<PubMedIdentifier>) =
        produce<PubMedEntry> {
            for(identifier in identifiers){
               // logger.atInfo().log("Retrieving data for ${identifier.pubmedId}")
                val entry = CosmicPubMedArticleLoader.retrievePubMedData(identifier)
                if (entry != null) {
                    send(entry)
                    delay(333)  // NCBI max request rate with key
                }
            }
        }

    /*
    Function to load retrieved data into the Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadPubMedEntries(entries: ReceiveChannel<PubMedEntry>) =
        produce<PubMedEntry> {
            for (entry in entries){
                PubMedArticleDao.mergePubMedEntry(entry)
                    send(entry)
                    delay(20)
            }
        }

    /*
    Function to create empty reference nodes for novel PubMed articles
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processArticleReferences (entries: ReceiveChannel<PubMedEntry>) =
        produce<Int> {
            for(entry in entries){
                if(entry.parentPubMedId == 0) {
                    // only process references for origin nodes
                    val parentId = entry.pubmedId
                    entry.referenceSet.stream()
                        .map { resolveReferenceIdentifier(it, parentId) }
                        .forEach { PlaceholderNodeLoader.processSingleIdentifier(it) }
                }
                send(entry.pubmedId)
                delay(10)
            }
        }

/*
Private function to resolve a PubMedIdentifier for a Reference node
 */
    private fun resolveReferenceIdentifier(pubmedId: Int, parentId: Int) =
        PubMedIdentifier(pubmedId,parentId,"Reference")

    /*
    Private function to request data from NCBI
     */
    private fun retrievePubMedData(identifier: PubMedIdentifier): PubMedEntry? {
        return when (val retEither = PubMedRetrievalService.retrievePubMedArticle(identifier.pubmedId)) {
            is Either.Right -> {
                val pubmedArticle = retEither.value
                PubMedEntry.parsePubMedArticle(pubmedArticle, identifier.label, identifier.parentId)
            }
            is Either.Left -> {
                logger.atSevere().log(retEither.value.message)
                null
            }
        }
    }
 fun scheduledPlaceHolderNodeScan(interval: Long = 60_000): TimerTask {
        val fixedRateTimer = Timer().scheduleAtFixedRate(delay = 5_000, period = interval) {
           processPlaceholderNodes()
        }
        return fixedRateTimer
    }

    fun processPlaceholderNodes() = runBlocking{
        // load complete the CosmicArticle placeholder nodes and then
        // complete their reference nodes
        var nodeCount = 0
        var cycleCount = 0
        logger.atInfo().log("Completing placeholder PubMedArticle nodes")
        val stopwatch = Stopwatch.createStarted()
        repeat(2) {
            cycleCount += 1
            //logger.atInfo().log("+++++ Initiating cycle # $cycleCount")
            val  ids = processArticleReferences(
                loadPubMedEntries(
                    retrievePubMedData(
                        getPlacholders()
                    )
                )
            )
            for (id in ids){
                nodeCount += 1
            }
            delay(100)
        }
        logger.atInfo().log("CosmicPubMedArticleLoader data loaded " +
                " $nodeCount nodes in " +
                " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds")
    }
}

fun main(args: Array<String>) {
   val pubmedidColumn =
        DatafilePropertiesService.resolvePropertyAsString("file.cosmic.export.pubmedid.column")
    val defaultSampleFile =
        DatafilePropertiesService.resolvePropertyAsString("file.sample.cosmic.mutant.export.census")
    val filename =  when (args.isNotEmpty()){
        true -> args[0]
        false -> defaultSampleFile
    }
    val colName  = when (args.size > 1) {
        true -> args[1]
        false -> pubmedidColumn
    }
    val label = when (args.size > 2) {
        true -> args[2]
        false -> "CosmicArticle"
    }
    // load the initial placeholder PubMedArticle nodes
   // PlaceholderNodeLoader.processTsvFile(filename,colName,label)
    // complete those nodes and process their references
   // CosmicPubMedArticleLoader.processPlaceholderNodes()
    val scanTimer = CosmicPubMedArticleLoader.scheduledPlaceHolderNodeScan(10_000)
    try {
        Thread.sleep(240_000)
    } finally {
        scanTimer.cancel();
    }
}