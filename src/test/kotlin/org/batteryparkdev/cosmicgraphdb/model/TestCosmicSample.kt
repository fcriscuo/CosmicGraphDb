package org.batteryparkdev.cosmicgraphdb.model

import com.google.common.base.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService
import org.batteryparkdev.io.CSVRecordSupplier
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
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
        val filename  = CosmicFilenameService.resolveCosmicCompleteFileLocation("CosmicSample.tsv")
        val records = produceCSVRecords(filename)
        val stopwatch = Stopwatch.createStarted()
        for (record in records) {
            nodeCount += 1
            val sample = CosmicSample.parseCSVRecord(record)
//            when (sample.isValid()) {
//                true -> println(
//                    "Sample Id: ${sample.sampleId}  Tumor Id: ${sample.cosmicTumor.tumorId} " +
//                            " Patient Id: ${sample.cosmicPatient.patientId}"
//                )
//                false -> println("Row $nodeCount is invalid")
//            }
        }
        println("Sample record count = $nodeCount  Time = ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
    }
}

fun main() = TestCosmicSample().testCosmicModel()
