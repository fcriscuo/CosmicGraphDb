package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.io.CsvRecordSequenceSupplier
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.nio.file.Paths

class TestCosmicClassification {
    var nodeCount = 0
    private val LIMIT = 400L

    fun testCosmicModel(): Unit{
        var nodeCount = 0
        val filename =  ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("classification.csv")
        println("Processing file: $filename")
        val path = Paths.get(filename)
        CsvRecordSequenceSupplier(path).get()
            .chunked(100)

            .forEach { recordList ->
                recordList.stream()
                    .map { record -> CosmicClassification.parseCSVRecord(record) }
                    .forEach { classification ->
                        nodeCount += 1
                        when (classification.isValid()) {
                            true -> println("PhenotypeId Id: ${classification.cosmicPhenotypeId}  NCI code: " +
                                    " ${classification.nciCode}")
                            false -> println("Row $nodeCount is invalid")
                        }
                    }
            }
        println("CosmicClassification record count = $nodeCount")
    }
}

fun main(args: Array<String>) {
    val test = TestCosmicClassification().testCosmicModel()

}