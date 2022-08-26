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

class TestCosmicPatient {

    private var nodeCount = 0
    private val LIMIT = 4000L

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get()
                .limit(LIMIT)
                .asSequence()
                .filter { it.size() > 1 }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun processCSVRecords() = runBlocking {
        val filename = CosmicFilenameService.resolveCosmicCompleteFileLocation("CosmicSample.tsv")
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
        println("Patient record count = $nodeCount")
    }
}

fun main() =
    TestCosmicPatient().processCSVRecords()