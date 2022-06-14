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

class TestCosmicSample {

    var nodeCount = 0

    fun parseCosmicSampleFile(filename: String): Int {
        ApocFileReader.processDelimitedFile(filename)
            .stream()
            .map { record -> record.get("map") }
            .map { CosmicSample.parseValueMap(it) }
            .forEach {sample->
                nodeCount += 1
                when (sample.isValid()) {
                    true -> println("CosmicSample ${sample.sampleId} " +
                            "  patient: ${sample.cosmicPatient.patientId}" )
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
            val sample = CosmicSample.parseCSVRecord(record)
            when (sample.isValid()) {
                true -> println(
                    "Sample Id: ${sample.sampleId}  Tumor Id: ${sample.cosmicTumor.tumorId} " +
                            " Patient Id: ${sample.cosmicPatient.patientId}"
                )
                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main() {
    val filename  = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicSample.tsv")
    val test = TestCosmicSample()
    test.processCSVRecords(filename)
    println("Sample record count = ${test.nodeCount}")
}