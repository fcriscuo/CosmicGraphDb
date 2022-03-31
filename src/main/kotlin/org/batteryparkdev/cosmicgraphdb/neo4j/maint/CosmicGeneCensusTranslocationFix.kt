package org.batteryparkdev.cosmicgraphdb.neo4j.maint

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicGeneDao.loadTranslocPartnerList
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService
import org.batteryparkdev.io.CsvRecordSequenceSupplier
import java.nio.file.Paths

/*
Responsible for refactoring the existing Neo4j database by changing
translocation data from CosmicGene annotations to a HAS_TRANSLOCATION_PARTNER
relationship between two (2) CosmicGene nodes
This code only needed to be invoked for the original database load
The codebase no longer creates annotations for translocations
 */
object CosmicGeneCensusTranslocationFix {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicGeneCensusFile(cosmicGeneCensusFile: String) =
        produce<CosmicGeneCensus> {
            val path = Paths.get(cosmicGeneCensusFile)
            CsvRecordSequenceSupplier(path).get()
                .forEach {
                    send (CosmicGeneCensus.parseCsvRecord(it))
                    delay(20)
                }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadTranslocPartnerList( genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<String> {
            for (gene in genes) {
                println("Loading translocation partners for gene: ${gene.geneSymbol}")
                loadTranslocPartnerList(gene.geneSymbol,gene.translocationPartnerList)
                send(gene.geneSymbol)
                delay(20)
            }
        }

    fun loadCosmicGeneTransLocationData(filename: String) =  runBlocking {
        logger.atInfo().log("Loading CosmicGeneCensus translocation data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val geneSymbols =
            loadTranslocPartnerList(
                    parseCosmicGeneCensusFile(filename)
                                       )
        for (symbol in geneSymbols) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicGeneCensus data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )
    }
}
/*
main function for integration testing
 */
fun main(args: Array<String>) {
    val fileDirectory = DatafilePropertiesService.resolvePropertyAsString("cosmic.sample.data.directory")
    val defaultFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.gene.census")
    val filename = if (args.isNotEmpty()) args[0] else defaultFile
    CosmicGeneCensusTranslocationFix.loadCosmicGeneTransLocationData(filename)
}