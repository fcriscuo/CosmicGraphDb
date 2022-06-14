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

class TestCosmicHGNC {
    var nodeCount = 0
    fun parseCosmicHGNCFile(filename: String): Int {
        // limit the number of records processed
        val LIMIT = Long.MAX_VALUE

        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicHGNC.parseValueMap(it) }
            .forEach { hgnc ->
                nodeCount += 1
                when (hgnc.isValid()) {
                    true -> println(
                        "CosmicHGNC: ${hgnc.hgncId} " +
                                "  gene symbol: ${hgnc.hgncGeneSymbol}"
                    )

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
            val hgnc = CosmicHGNC.parseCSVRecord(record)
            when (hgnc.isValid()) {
                true -> println(
                    "Gene symbol: ${hgnc.hgncGeneSymbol}  HGNC Id: ${hgnc.hgncId} " +
                            " COSMIC Id: ${hgnc.cosmicId}"
                )

                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicHGNC.tsv")
    val test = TestCosmicHGNC()
    test.processCSVRecords(filename)
    println("HGNC record count = ${test.nodeCount}")
}