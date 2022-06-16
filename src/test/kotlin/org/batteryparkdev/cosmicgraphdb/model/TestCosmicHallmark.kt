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

class TestCosmicHallmark: TestCosmicModel() {


    var nodeCount = 0

    fun parseCosmicHallmarkFile(filename: String): Int {

        ApocFileReader.processDelimitedFile(filename)
            .stream()
            .map { record -> record.get("map") }
            .map { CosmicHallmark.parseValueMap(it) }
            .forEach { hall ->
                nodeCount += 1
                when (hall.isValid()) {
                    true -> println("Hallmark Id: ${hall.hallmarkId} " +
                        "CosmicHallmark: ${hall.geneSymbol} " +
                                "  Hallmark: ${hall.hallmark} "
                    )

                    false -> println("Row $nodeCount is invalid")
                }
            }
        return nodeCount
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun CoroutineScope.produceCSVRecords(filename: String) =
//        produce<CSVRecord> {
//            val path = Paths.get(filename)
//            CSVRecordSupplier(path).get().asSequence()
//                .filter { it.size() > 1 }
//                .forEach {
//                    send(it)
//                    delay(20)
//                }
//        }
    /*
    1807793882
     */

    fun processCSVRecords(filename: String) = runBlocking {
        val records = produceCSVRecords(filename)
        for (record in records) {
            nodeCount += 1
            val hallmark = CosmicHallmark.parseCSVRecord(record)
            when (hallmark.isValid()) {
                true -> println("Hallmark Id: ${hallmark.hallmarkId} " +
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
        ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    TestCosmicHallmark().let {
        it.processCSVRecords(cosmicHallmarkFile)
       // it.parseCosmicHallmarkFile(cosmicHallmarkFile)
        println("Loaded COSMIC gene census hallmark file: $cosmicHallmarkFile  record count = ${it.nodeCount}")
    }
}
