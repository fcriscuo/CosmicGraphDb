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

class TestCosmicResistanceMutation {

    private val LIMIT = 4000L
    var nodeCount = 0

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
        val filename = CosmicFilenameService.resolveCosmicCompleteFileLocation("CosmicResistanceMutations.tsv")
        val records = produceCSVRecords(filename)
        for (record in records) {
            nodeCount += 1
            val mutation = CosmicResistanceMutation.parseCSVRecord(record)
            when (mutation.isValid()) {
                true -> println(
                    "Mutation Id: ${mutation.mutationId}  Gene Symbol: ${mutation.geneSymbol} " +
                            " Drug: ${mutation.drugName}"
                )
                false -> println("Row $nodeCount is invalid")
            }
        }
        println("CosmicResistanceMutations record count = $nodeCount")
    }
}

fun main() =
     TestCosmicResistanceMutation().testCosmicModel()