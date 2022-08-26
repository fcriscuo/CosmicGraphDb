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

class TestCosmicGeneCensus {

    private var nodeCount = 0

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get().asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun testCosmicModel() = runBlocking {
        val filename = CosmicFilenameService.resolveCosmicCompleteFileLocation("cancer_gene_census.csv")
        val records = produceCSVRecords(filename)
        for (record in records) {
            nodeCount += 1
            val gene = CosmicGeneCensus.parseCSVRecord(record)
            when (gene.isValid()) {
                true -> println("Gene symbol: ${gene.geneSymbol}  Gene name: ${gene.geneName} " +
                        " Genome location: ${gene.genomeLocation}")
                false -> println("Row $nodeCount is invalid")
            }
        }
        println("Cosmic Gene Census node count = $nodeCount")
    }
}

fun main() =
    TestCosmicGeneCensus().testCosmicModel()
