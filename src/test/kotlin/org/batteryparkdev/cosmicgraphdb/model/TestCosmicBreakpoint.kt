package org.batteryparkdev.cosmicgraphdb.model


import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.nio.file.Paths

class TestCosmicBreakpoint {
    private val LIMIT = Long.MAX_VALUE

    /*
    Test parsing sample Cosmic breakpoints TSV file and mapping data to
    CosmicBreakpoint model class
    n.b. file name specification must be full path since it is resolved by Neo4j server
     */
    fun parseBreakpointFile(filename: String): Int {
        // limit the number of records processed
        var nodeCount = 0
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicBreakpoint.parseValueMap(it) }
            .forEach { breakpoint ->
                nodeCount += 1
                when (breakpoint.isValid()) {
                    true -> println("CosmicBreakpoint: ${breakpoint.mutationId}  sample: ${breakpoint.sampleId}")
                    false -> println("Row $nodeCount is invalid")
                }

            }
        return nodeCount
    }

    fun csvParseBreakpointFile(filename: String): Int {
        var nodeCount = 0
        println("Processing file: $filename")
        val path = Paths.get(filename)
        TsvRecordSequenceSupplier(path).get().chunked(100)
            .forEach { recordList ->
                recordList.stream()
                    .map { record -> CosmicBreakpoint.parseCSVRecord(record) }
                    .forEach { brekpoint ->
                        nodeCount += 1
                        when (brekpoint.isValid()) {
                            true -> println("Sample Id: ${brekpoint.sampleId}  Mutation Id" +
                                    " ${brekpoint.mutationId}")
                            false -> println("Row $nodeCount is invalid")
                        }
                    }
            }
        return nodeCount
    }
}

fun main() {
    val cosmicBreakpointFile =  ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicBreakpointsExport.tsv")
val recordCount =
    TestCosmicBreakpoint().csvParseBreakpointFile(cosmicBreakpointFile)
    println("Breakpoint record count = $recordCount")
}