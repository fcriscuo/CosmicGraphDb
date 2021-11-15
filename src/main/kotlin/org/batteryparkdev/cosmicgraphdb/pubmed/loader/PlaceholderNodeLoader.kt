package org.batteryparkdev.cosmicgraphdb.pubmed.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.pubmed.dao.PubMedArticleDao
import org.batteryparkdev.cosmicgraphdb.pubmed.model.PubMedIdentifier
import java.nio.file.Paths

/*
Responsible for creating a set of empty (i.e. placeholder) PubMedArticle
nodes in the Neo4j database. This allows other data loaders to establish
relationships with PubMed nodes without being impacted by NCBI maximum
request rates. The PubMedArticle nodes will be completed using an
asynchronous process.
 */
object PlaceholderNodeLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    /*
    Function to create a channel of PubMed Ids (Int) from a specified column
    in a TSV-formatted file
    Default PubMed Id column name id Pubmed_PMID
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.produceIdChannel(
        cosmicTsvFile: String, pubmedIdCol: String = "Pubmed_PMID",
        label: String = "CosmicArticle"
    ) =
        produce<PubMedIdentifier> {
            val path = Paths.get(cosmicTsvFile)
            TsvRecordSequenceSupplier(path).get()
                .map { it.get(pubmedIdCol) }
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
                .filter { !PubMedArticleDao.pubMedNodeExistsPredicate(it) }
                .map { PubMedIdentifier(it, 0, label) }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    /*
    Function to create an empty PubMedArticle nodes
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.createPubMedArticle(identifiers: ReceiveChannel<PubMedIdentifier>) =
        produce<PubMedIdentifier> {
            for (identifier in identifiers) {
                PubMedArticleDao.createPlaceholderNode(identifier)
                send(identifier)
                delay(20)
            }
        }

    /*
    Public utility function to create a single placeholder node
     */
    fun processSingleIdentifier(identifier: PubMedIdentifier) {
        if (identifier.pubmedId > 0) {
            PubMedArticleDao.createPlaceholderNode(identifier)
        }
    }

    /*
    Public function to initiate processing a TSV-formatted file that contains a specified
    PubMed Id column
     */
    fun processTsvFile(filename: String, colName: String, label: String) = runBlocking {
        logger.atInfo().log("Loading PubMed Ids data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids =
                createPubMedArticle(
                    produceIdChannel(filename, colName, label)
                )

        for (id in ids) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "PubMed Id placeholder data loaded " +
                    " $nodeCount placeholder nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )
    }
}
/*
main function for Neo4j integration testing
 */
fun main (args: Array<String>) {
    val filename =  when (args.isNotEmpty()){
        true -> args[0]
        false -> "./data/sample_CosmicMutantExportCensus.tsv"
    }
    val colName  = when (args.size > 1) {
        true -> args[1]
        false -> "Pubmed_PMID"
    }
    val label = when (args.size > 2) {
        true -> args[2]
        false -> "CosmicArticle"
    }
    PlaceholderNodeLoader.processTsvFile(filename,colName,label)
    println("FINIS....")
}