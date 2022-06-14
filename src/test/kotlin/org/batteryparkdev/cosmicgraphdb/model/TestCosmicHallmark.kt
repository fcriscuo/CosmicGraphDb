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

class TestCosmicHallmark {

    private val LIMIT = Long.MAX_VALUE
    var nodeCount = 0

    fun parseCosmicHallmarkFile(filename: String): Int {
        // limit the number of records processed

        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicHallmark.parseValueMap(it) }
            .forEach { hall ->
                nodeCount += 1
                when (hall.isValid()) {
                    true -> println(
                        "CosmicHallmark: ${hall.geneSymbol} " +
                                "  Hallmark: ${hall.hallmark}"
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
            val hallmark = CosmicHallmark.parseCSVRecord(record)
            when (hallmark.isValid()) {
                true -> println(
                    "Gene symbol: ${hallmark.geneSymbol}  Hallmark: ${hallmark.hallmark} " +
                            " Description: ${hallmark.description}"
                )

                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main() {
    val cosmicHallmarkFile =
        ConfigurationPropertiesService.resolveCosmicSampleFileLocation("Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    val test = TestCosmicHallmark()
    //test.processCSVRecords(cosmicHallmarkFile)
    test.parseCosmicHallmarkFile(cosmicHallmarkFile)
    println("Loaded COSMIC gene census hallmark file: $cosmicHallmarkFile  record count = ${test.nodeCount}")
}
