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

class TestCosmicGeneCensus {
    private val LIMIT = Long.MAX_VALUE

    /*
    n.b. file name specification must be a full path since it is resolved by the Neo4j server
     */
    fun parseCosmicGeneCensusFile(filename: String): Int {
       var nodeCount = 0
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicGeneCensus.parseValueMap(it) }
            .forEach { gene ->
                nodeCount += 1
                when (gene.isValid()) {
                    true -> println("CosmicGeneCensus: ${gene.geneSymbol} " +
                            "  name: ${gene.geneName}" )
                    false -> println("Row $nodeCount is invalid")
                }
            }
        println("Node count: $nodeCount")
        return nodeCount
    }

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

    fun processCSVRecords(filename: String) = runBlocking {
        val records = produceCSVRecords(filename)
        var nodeCount = 0
        for (record in records) {
            nodeCount += 1
            val gene = CosmicGeneCensus.parseCSVRecord(record)
            when (gene.isValid()) {
                true -> println("Gene symbol: ${gene.geneSymbol}  Gene name: ${gene.geneName} " +
                        " Genome location: ${gene.genomeLocation}")
                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main() {
    val cosmicGeneCensusFile = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("cancer_gene_census.csv")
    //TestCosmicGeneCensus().parseCosmicGeneCensusFile(cosmicGeneCensusFile)
    TestCosmicGeneCensus().processCSVRecords(cosmicGeneCensusFile)
}