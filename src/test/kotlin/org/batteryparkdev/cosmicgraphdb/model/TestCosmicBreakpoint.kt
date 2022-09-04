package org.batteryparkdev.cosmicgraphdb.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService
import org.batteryparkdev.genomicgraphcore.common.io.CSVRecordSupplier
import java.nio.file.Paths
import kotlin.streams.asSequence

class TestCosmicBreakpoint {
    var nodeCount = 0
    private val LIMIT = 4000L

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get().limit(LIMIT).asSequence()
                .filter { it.size() > 1 }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    fun testCosmicModel() = runBlocking {
        val cosmicBreakpointFile =
            CosmicFilenameService.resolveCosmicCompleteFileLocation("CosmicBreakpointsExport.tsv")
        val records = produceCSVRecords(cosmicBreakpointFile)
        for (record in records) {
            nodeCount += 1
            val breakpoint = CosmicBreakpoint.createCoreModelFunction.invoke(record)
            when (breakpoint.isValid()) {
                true -> println("Sample Id: ${breakpoint.getModelSampleId()}  Mutation Id" +
                            " ${breakpoint.getNodeIdentifier().idValue}")
                false -> println("Row $nodeCount is invalid")
            }
        }
        println("Breakpoint record count = $nodeCount")
    }
}

fun main() {
    TestCosmicBreakpoint().let {
        it.testCosmicModel()
    }
}