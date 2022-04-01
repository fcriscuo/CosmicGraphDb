package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.cosmic.dao.*
import org.batteryparkdev.cosmicgraphdb.model.CosmicTumor
import org.batteryparkdev.cosmicgraphdb.dao.*
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object CosmicTumorLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicMutantExportCensusFile(mutantExportFile: String) =
        produce<CosmicTumor> {
            val path = Paths.get(mutantExportFile)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicTumor.parseCsvRecord(it))
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicTumors(tumors: ReceiveChannel<CosmicTumor>) =
        produce<CosmicTumor> {
            for (tumor in tumors) {
                loadCosmicTumor(tumor)
                send(tumor)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processTumorSiteType(tumors: ReceiveChannel<CosmicTumor>) =
        produce<CosmicTumor> {
            for (tumor in tumors) {
                CosmicTypeDao.processCosmicTypeNode(tumor.site)
                send(tumor)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processTumorHistologyType(tumors: ReceiveChannel<CosmicTumor>) =
        produce<CosmicTumor> {
            for (tumor in tumors) {
                CosmicTypeDao.processCosmicTypeNode(tumor.histology)
                send(tumor)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processSampleRelationships(tumors: ReceiveChannel<CosmicTumor>) =
        produce<CosmicTumor> {
            for (tumor in tumors) {
                createCosmicSampleRelationship(tumor)
                send(tumor)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processMutationRelationships(tumors: ReceiveChannel<CosmicTumor>) =
        produce<CosmicTumor> {
            for (tumor in tumors) {
                createCosmicMutationRelationship(tumor)
                send(tumor)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processPubMedRelationships(tumors: ReceiveChannel<CosmicTumor>) =
        produce<Int> {
            for (tumor in tumors) {
                createPubMedRelationship(tumor)
                send(tumor.tumorId)
                delay(20)
            }
        }

    fun processCosmicTumorData(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicMutantExport data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = processPubMedRelationships(
            processMutationRelationships(
                processSampleRelationships(
                    processTumorHistologyType(
                        processTumorSiteType(
                            loadCosmicTumors(
                                parseCosmicMutantExportCensusFile(filename)
                            )
                        )
                    )
                )
            )
        )
        for (id in ids) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicMutantExport data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }

}

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/sample_CosmicMutantExportCensus.tsv"
    CosmicTumorLoader.processCosmicTumorData(filename)
}