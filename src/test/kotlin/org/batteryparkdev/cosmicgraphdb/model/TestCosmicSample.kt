package org.batteryparkdev.cosmicgraphdb.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.CSVRecordSupplier
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.nio.file.Paths
import kotlin.streams.asSequence

class TestCosmicSample {

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

    fun testCosmicModel() = runBlocking {
        val filename  = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicSample.tsv")
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
        println("Sample record count = $nodeCount")
    }
}

fun main() = TestCosmicSample().testCosmicModel()
