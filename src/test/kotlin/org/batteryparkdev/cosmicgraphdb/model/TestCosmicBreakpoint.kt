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

class TestCosmicBreakpoint {
    var nodeCount = 0

    /*
    APOC parser
     */
    fun parseBreakpointFile(filename: String): Unit {
        ApocFileReader.processDelimitedFile(filename)
            .stream()
            .map { record -> record.get("map") }
            .map { CosmicBreakpoint.parseValueMap(it) }
            .forEach { breakpoint ->
                nodeCount += 1
                when (breakpoint.isValid()) {
                    true -> println("CosmicBreakpoint: ${breakpoint.mutationId}  sample: ${breakpoint.sampleId}")
                    false -> println("Row $nodeCount is invalid")
                }
            }
    }

    /*
    Apache Commons CSV parser
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.produceCSVRecords(filename: String) =
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
            val breakpoint = CosmicBreakpoint.parseCSVRecord(record)
            when (breakpoint.isValid()) {
                true -> println("Sample Id: ${breakpoint.sampleId}  Mutation Id" +
                            " ${breakpoint.mutationId}")
                false -> println("Row $nodeCount is invalid")
            }
        }
    }
}

fun main() {
    val cosmicBreakpointFile =
        ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicBreakpointsExport.tsv")
    TestCosmicBreakpoint().let {
        it.processCSVRecords(cosmicBreakpointFile)
        println("Breakpoint record count = ${it.nodeCount}")
    }
}