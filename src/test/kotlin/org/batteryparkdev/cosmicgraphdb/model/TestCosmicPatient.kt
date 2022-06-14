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

class TestCosmicPatient {

    var nodeCount = 0

    fun loadCosmicPatientFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicPatient.parseValueMap(it) }
            .forEach { patient ->
                nodeCount += 1
                when (patient.isValid()) {
                    true -> println(
                        "CosmicPatient: ${patient.patientId} " +
                                "  tumor id: ${patient.tumorId}"
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
            val patient = CosmicPatient.parseCSVRecord(record)
            when (patient.isValid()) {
                true -> println(
                    "Patient Id: ${patient.patientId}  Tumor Id: ${patient.tumorId} " +
                            " Sample Id: ${patient.sampleId}"
                )

                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicSample.tsv")
    val test = TestCosmicPatient()
    test.processCSVRecords(filename)
    println("Patient record count = ${test.nodeCount}")
}