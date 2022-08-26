package org.batteryparkdev.cosmicgraphdb.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService
import org.batteryparkdev.io.CSVRecordSupplier
import java.nio.file.Paths
import kotlin.streams.asSequence

class TestCosmicCompleteCNA {
    var nodeCount = 0
    private val LIMIT = 4000L
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceTSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get()
                .limit(LIMIT)
                .asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun testCosmicModel() = runBlocking {
        val filename = CosmicFilenameService.resolveCosmicCompleteFileLocation("CosmicCompleteCNA.tsv")
        val records = produceTSVRecords(filename)
        for (record in records) {
            nodeCount += 1
            val cna = CosmicCompleteCNA.parseCSVRecord(record)
            when (cna.isValid()) {
                true -> println("Gene Symbol: ${cna.geneSymbol}  Sample Id: ${cna.sampleId}")
                false -> println("Row $nodeCount is invalid")
            }
        }
        println("COSMIC CNA record count = $nodeCount")
    }
}

fun main() {
    TestCosmicCompleteCNA().let {
        it.testCosmicModel()

    }

}