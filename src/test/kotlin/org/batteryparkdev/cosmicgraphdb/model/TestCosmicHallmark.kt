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

class TestCosmicHallmark {

    private var nodeCount = 0
    private val LIMIT = 4000L

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get()
                .limit(LIMIT)
                .asSequence()
                .filter { it.size() > 1 }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun testCosmicModel() = runBlocking {
        val filename = CosmicFilenameService
            .resolveCosmicCompleteFileLocation("Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
        val records = produceCSVRecords(filename)
        for (record in records) {
            nodeCount += 1
            val hallmark = CosmicHallmark.parseCSVRecord(record)
            when (hallmark.isValid()) {
                true -> println(
                    "Hallmark Id: ${hallmark.hallmarkId} " +
                            "Gene symbol: ${hallmark.geneSymbol}  Hallmark: ${hallmark.hallmark} " +
                            " Description: ${hallmark.description}"
                )

                false -> println("Row $nodeCount is invalid")
            }
        }
        println("COSMIC gene census hallmark record count = $nodeCount")
    }
}

fun main() = TestCosmicHallmark().testCosmicModel()

