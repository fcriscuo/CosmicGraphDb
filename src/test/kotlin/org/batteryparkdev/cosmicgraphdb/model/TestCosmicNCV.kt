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

class TestCosmicNCV {

    private val LIMIT = 4000L
    private var nodeCount = 0

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
        val filename = ConfigurationPropertiesService.resolveCosmicCompleteFileLocation("CosmicNCV.tsv")
        val records = produceCSVRecords(filename)
        for (record in records) {
            nodeCount += 1
            val ncv = CosmicNCV.parseCSVRecord(record)
            when (ncv.isValid()) {
                true -> println(
                    "Sample Id: ${ncv.sampleId}  Genomic Mutation Id: ${ncv.genomicMutationId} " +
                            " Mut Seq: ${ncv.mutSeq}"
                )

                false -> println("Row $nodeCount is invalid")
            }
        }
        println("COSMIC NCV file record count = $nodeCount")
    }
}

fun main() = TestCosmicNCV().testCosmicModel()
