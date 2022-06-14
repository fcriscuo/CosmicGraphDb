package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.io.CsvRecordSequenceSupplier
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.nio.file.Paths

class TestCosmicClassification {
    fun parseClassificationFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE

        var nodeCount = 0
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicClassification.parseValueMap(it) }
            .forEach {classification ->
                nodeCount += 1
                when (classification.isValid()) {
                    true -> println("CosmicClassification: ${classification.cosmicPhenotypeId} " +
                            "  NCI code: ${classification.nciCode}")
                    false -> println("Row $nodeCount is invalid")
                }
            }
        return nodeCount
    }

    fun csvParseClassificationFile(filename: String): Int {
        var nodeCount = 0
        println("Processing file: $filename")
        val path = Paths.get(filename)
        CsvRecordSequenceSupplier(path).get().chunked(100)
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
        return nodeCount
    }
}

fun main(args: Array<String>) {
    println("Using Neo4j server at ${System.getenv("NEO4J_URI")}")
    val cosmicClassificationFile =  ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("classification.csv")
    println("Loading data from file: $cosmicClassificationFile")
    val test = TestCosmicClassification()
    val recordCount = test.csvParseClassificationFile(cosmicClassificationFile)
    println("Loaded CosmicClassification  record count = $recordCount")
}