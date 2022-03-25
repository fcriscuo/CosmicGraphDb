package org.batteryparkdev.cosmicgraphdb.poc

import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicTumor
import org.batteryparkdev.io.TsvRecordChannel

object CosmicMutantExportChannel {

    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.getRecords(filename: String) =
        produce<CSVRecord> {
            val flow = TsvRecordChannel.produceTSVRecordFlow(filename)
            flow.collect {
                send(it)
            }
        }

    // Tumor data from CosmicMutantExport
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.parseCosmicTumors (records: ReceiveChannel<CSVRecord>) =
        produce<CosmicTumor> {
            for (record in records) {
                println("Parsing CosmicTumor TSV record")
                send(CosmicTumor.parseCsvRecord(record))
                delay(10)
            }
        }
    // Mutation data from CosmicMutantExport
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.parseCosmicMutations (records: ReceiveChannel<CSVRecord>) =
        produce<CosmicMutation> {
            for (record in records) {
                println("Parsing CosmicMutation TSV record")
                send(CosmicMutation.parseCsvRecord(record))
                delay(10)
            }
        }

    fun processTumors(filename: String) = runBlocking {
        val tumors = parseCosmicTumors(
            getRecords(filename))
        val mutations = parseCosmicMutations(
            getRecords(filename)
        )
        for (tumor in tumors) {
            println("Tumor: ${tumor.tumorId}")
        }
        for (mutation in mutations) {
            println("Mutation: ${mutation.geneSymbol}")
        }
    }

}

fun main() {
    CosmicMutantExportChannel.processTumors("./data/sample_CosmicMutantExportCensus.tsv")
}