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

class TestCosmicCompleteExpression {
    private var nodeCount = 0
    private val LIMIT = 4000L

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceCSVRecords(filename: String) =
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
        val filename = CosmicFilenameService.resolveCosmicCompleteFileLocation("CosmicCompleteGeneExpression.tsv")
        val records = produceCSVRecords(filename)

        for (record in records) {
            nodeCount += 1
            val expression = CosmicCompleteGeneExpression.parseCSVRecord(record)
            when (expression.isValid()) {
                true -> println("Gene Symbol: ${expression.geneSymbol}  Sample Id: ${expression.sampleId}")
                false -> println("Row $nodeCount is invalid")
            }
        }
        println("Processed CosmicCompleteExpression  record count = $nodeCount")
    }
}

fun main (args:Array<String>) =
        TestCosmicCompleteExpression().testCosmicModel()
