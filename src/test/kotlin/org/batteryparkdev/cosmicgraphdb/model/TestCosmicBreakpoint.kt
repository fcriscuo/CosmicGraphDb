package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService
import org.batteryparkdev.neo4j.service.Neo4jConnectionService

class TestCosmicBreakpoint {
    /*
    Test parsing sample Cosmic breakpoints TSV file and mapping data to
    CosmicBreakpoint model class
    n.b. file name specification must be full path since it is resolved by Neo4j server
     */
    fun parseBreakpointFile(filename: String): Int {
        val LIMIT = 100L // limit the number of records processed
        var recordCount = 0
//        val cypher = "CALL apoc.load.csv(\"$filename\")," +
//                "{limit: $LIMIT, sep ='\t }" +
//                "YIELD lineNo, map RETURN map;"
//        val records = Neo4jConnectionService.executeCypherQuery(cypher);
        ApocFileReader.processDelimitedFile(filename)
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicBreakpoint.parseValueMap(it) }
            .forEach { breakpoint ->
                println(
                    "Breakpoint Mutation Id= ${breakpoint.mutationId}  " +
                            "  Tumor Id = ${breakpoint.tumorId}   PubMed Id: ${breakpoint.pubmedId}\n" +
                            "     From: ${breakpoint.chromosomeFrom}  ${breakpoint.locationFromMin} " +
                            "  ${breakpoint.locationFromMax}   ${breakpoint.strandFrom} \n" +
                            "     To: ${breakpoint.chromosomeTo}  ${breakpoint.locationToMin} " +
                            "  ${breakpoint.locationToMax}   ${breakpoint.strandTo} \n"

                )
                recordCount += 1
            }
        return recordCount
    }
}

fun main() {
    //cosmic.sample.data.directory
    //file.cosmic.breakpoints.export
    val dataDirectory = DatafilePropertiesService.resolvePropertyAsString("cosmic.sample.data.directory")
    val cosmicBreakpointsFile = dataDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.breakpoints.export")
    println("Processing COSMIC breakpoints file $cosmicBreakpointsFile")
    val recordCount = TestCosmicBreakpoint().parseBreakpointFile(cosmicBreakpointsFile)
    println("Record count = $recordCount")
}