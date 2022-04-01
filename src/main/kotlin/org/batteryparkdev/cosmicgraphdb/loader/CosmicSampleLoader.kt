package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.dao.CosmicTypeDao
import org.batteryparkdev.cosmicgraphdb.dao.addCosmicSampleTypeLabel
import org.batteryparkdev.cosmicgraphdb.dao.createCosmicSampleRelationships
import org.batteryparkdev.cosmicgraphdb.dao.loadCosmicSample
import org.batteryparkdev.cosmicgraphdb.model.CosmicSample
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/*
Responsible for loading data from the CosmicSample file into a Neo4j database
Creates noses, labels, and relationships
 */
object CosmicSampleLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicSampleFile(cosmicSampleFile: String) =
        produce<CosmicSample> {
            val path = Paths.get(cosmicSampleFile)
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    send(CosmicSample.parseCsvRecord(it))
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicSamples(samples: ReceiveChannel<CosmicSample>) =
        produce<CosmicSample> {
            for (sample in samples) {
                loadCosmicSample(sample)
                send(sample)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.addSampleLabel(samples: ReceiveChannel<CosmicSample>) =
        produce<CosmicSample> {
            for (sample in samples) {
                if (sample.sampleType.isNotEmpty()) {
                    addCosmicSampleTypeLabel(sample.sampleId, sample.sampleName)
                }
                send(sample)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadSampleSiteType(samples: ReceiveChannel<CosmicSample>) =
        produce<CosmicSample> {
            for (sample in samples) {
                CosmicTypeDao.processCosmicTypeNode(sample.site)
                send(sample)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadSampleHistologyType(samples: ReceiveChannel<CosmicSample>) =
        produce<CosmicSample> {
            for (sample in samples) {
                CosmicTypeDao.processCosmicTypeNode(sample.histology)
                send(sample)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.createSampleRelationships(samples: ReceiveChannel<CosmicSample>) =
        produce<Int> {
            for (sample in samples) {
                createCosmicSampleRelationships(sample)
                send(sample.sampleId)
                delay(20)
            }
        }

    fun processCosmicSampleData(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicSample data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val ids = createSampleRelationships(
            loadSampleHistologyType(
                loadSampleSiteType(
                    addSampleLabel(
                        loadCosmicSamples(
                            parseCosmicSampleFile(filename)
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
            "CosmicSample data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds"
        )
    }
}
// integration test using truncated sample file
fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/sample_CosmicSample.tsv"
    CosmicSampleLoader.processCosmicSampleData(filename)
}
