package org.batteryparkdev.cosmicgraphdb.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.io.CSVRecordSupplier
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.nio.file.Paths
import kotlin.streams.asSequence

class TestCosmicResistanceMutation {
    private val LIMIT = Long.MAX_VALUE
    var nodeCount = 0

    fun parseCosmicResistanceMutationFile(filename: String): Int {
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicResistanceMutation.parseValueMap(it) }
            .forEach { mutation ->
                nodeCount += 1
                when (mutation.isValid()) {
                    true -> println("CosmicResistanceMutation: ${mutation.mutationId}  drug: ${mutation.drugName}")
                    false -> println("Row $nodeCount is invalid")
                }
            }
        return nodeCount
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get().asSequence()
                .filter { it.size() > 1 }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun processCSVRecords(filename: String) = runBlocking {
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
    }
}

fun main() {
    val cosmicResistanceFile =
        ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicResistanceMutations.tsv")
    val test = TestCosmicResistanceMutation()
    test.processCSVRecords(cosmicResistanceFile)
    println("Processed $cosmicResistanceFile record count = ${test.nodeCount}")
}