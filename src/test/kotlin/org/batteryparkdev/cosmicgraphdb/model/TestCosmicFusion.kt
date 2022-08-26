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

class TestCosmicFusion {
    private val LIMIT = Long.MAX_VALUE
    private var nodeCount = 0

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get()
                .limit(LIMIT)
                .asSequence()
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun testCosmicModel() = runBlocking {
        val filename = CosmicFilenameService.resolveCosmicCompleteFileLocation("CosmicFusionExport.tsv")
        val records = produceCSVRecords(filename)
        for (record in records) {
            nodeCount += 1
            val fusion = CosmicFusion.parseCSVRecord(record)
            when (fusion.isValid()) {
                true -> println("Sample Id: ${fusion.sampleId}  5' gene ${fusion.five_geneSymbol} " +
                        " 3' gene ${fusion.three_geneSymbol}")
                false -> println("Row $nodeCount is invalid")
            }
        }
        println("CosmicFusion record count = $nodeCount")
    }
}
fun main() =
    TestCosmicFusion().testCosmicModel()
