package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService
import org.batteryparkdev.io.CSVRecordSupplier
import java.nio.file.Paths

class TestCosmicCodingMutation {
    private val LIMIT = 3000L

    fun testCosmicModel(): Unit {
        var nodeCount = 0
        val filename = CosmicFilenameService.resolveCosmicCompleteFileLocation("CosmicMutantExportCensus.tsv")
        println("Processing file: $filename")
        val path = Paths.get(filename)
        CSVRecordSupplier(path).get()
            .limit(LIMIT)
            .map { it -> CosmicCodingMutation.parseCSVRecord(it) }
            .forEach { mutation ->
                        nodeCount += 1
                        when (mutation.isValid()) {
                            true -> println("Mutation row: $nodeCount  Gene Symbol: ${mutation.geneSymbol}  AA mut ${mutation.mutationAA}")
                            false -> println("Row $nodeCount is invalid")
                        }
                    }

        println("Processed Mutation record count = $nodeCount")
    }
}

fun main() {
     TestCosmicCodingMutation().testCosmicModel()

}
