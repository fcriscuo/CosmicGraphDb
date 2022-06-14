package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.io.CSVRecordSupplier
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.nio.file.Paths

class TestCosmicCodingMutation {
    fun apocParseCosmicMutantExportFile(filename: String): Int {
        var nodeCount = 0
        println("Processing file: $filename")
        ApocFileReader.processDelimitedFile(filename)
            .map { record -> record.get("map") }
            .map { CosmicCodingMutation.parseValueMap(it) }
            .forEach { mutation ->
                nodeCount += 1
                when (mutation.isValid()) {
                    true -> println(
                        "CosmicCodingMutation: ${mutation.geneSymbol} " +
                                "  AA mutation: ${
                                    mutation.mutationAA
                                }"
                    )
                    false -> println("Row $nodeCount is invalid")
                }
            }
        return nodeCount
    }

    fun csvParseCosmicMutantExportFile(filename: String): Int {
        var nodeCount = 0
        println("Processing file: $filename")
        val path = Paths.get(filename)
        CSVRecordSupplier(path).get()
            .map { it -> CosmicCodingMutation.parseCSVRecord(it) }
            .forEach { mutation ->
                        nodeCount += 1
                        when (mutation.isValid()) {
                            true -> println("Gene Symbol: ${mutation.geneSymbol}  AA mut ${mutation.mutationAA}")
                            false -> println("Row $nodeCount is invalid")
                        }
                    }

        return nodeCount
    }
}

fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicMutantExportCensus.tsv")
    val recordCount =
        TestCosmicCodingMutation().csvParseCosmicMutantExportFile(filename)
    //TestCosmicCodingMutation().apocParseCosmicMutationFile(filename)
    println("Mutation record count = $recordCount")
}
