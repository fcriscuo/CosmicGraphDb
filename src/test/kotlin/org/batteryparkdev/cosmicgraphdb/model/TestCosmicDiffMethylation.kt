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

class TestCosmicDiffMethylation {
    fun parseCosmicCompleteDifferentialMethylation(filename: String): Int {
        val LIMIT = Long.MAX_VALUE
        var nodeCount = 0
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicDiffMethylation.parseValueMap(it) }
            .forEach {
                nodeCount += 1
                when (it.isValid()) {
                    true -> println(
                        "CosmicDiffMethylation: ${it.sampleId} " +
                                "  methylation: ${it.methylation}"
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
            val methylation = CosmicDiffMethylation.parseCSVRecord(record)
            when (methylation.isValid()) {
                true -> println("Sample: ${methylation.sampleId}  Methylation: ${methylation.methylation}")
                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main (args:Array<String>) {
    val cosmicMethylationFile =  ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicCompleteDifferentialMethylation.tsv")
    val recordCount =
       TestCosmicDiffMethylation().processCSVRecords(cosmicMethylationFile)
    println("Processed $cosmicMethylationFile  record count = $recordCount")
}