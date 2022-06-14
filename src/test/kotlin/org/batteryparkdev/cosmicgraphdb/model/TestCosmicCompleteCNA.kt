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

class TestCosmicCompleteCNA {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceTSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get().asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun processCsvRecords(filename: String) = runBlocking {
        val records = produceTSVRecords(filename)
        var nodeCount = 0
        for (record in records) {
            nodeCount += 1
            val cna = CosmicCompleteCNA.parseCSVRecord(record)
            when (cna.isValid()) {
                true -> println("Gene Symbol: ${cna.geneSymbol}  Sample Id: ${cna.sampleId}")
                false -> println("Row $nodeCount is invalid")
            }
        }
    }

    fun parseCNAFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE// limit the number of records processed
        var nodeCount = 0
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicCompleteCNA.parseValueMap(it) }
            .forEach { cna ->
                nodeCount += 1
                when (cna.isValid()) {
                    true -> println(
                        "CosmicCNA: ${cna.geneSymbol} " +
                                "  sample id: ${cna.sampleId}"
                    )
                    false -> println("Row $nodeCount is invalid")
                }
            }
        return nodeCount
    }


    fun cnaParseCosmicCompleteCNAFile(filename: String): Int {
        var nodeCount = 0
        println("Processing file: $filename")
        val path = Paths.get(filename)
        CSVRecordSupplier(path).get()
            .map { record -> CosmicCompleteCNA.parseCSVRecord(record) }
            .forEach { cna ->
                nodeCount += 1
                when (cna.isValid()) {
                    true -> println("Gene Symbol: ${cna.geneSymbol}  Sample Id: ${cna.sampleId}")
                    false -> println("Row $nodeCount is invalid")
                }
            }

        return nodeCount
    }
}

fun main() {
    val cosmicCNAFile = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicCompleteCNA.tsv")
    println("Processing COSMIC CNA file $cosmicCNAFile")
    val recordCount = TestCosmicCompleteCNA().processCsvRecords(cosmicCNAFile)
    println("Record count = $recordCount")
}