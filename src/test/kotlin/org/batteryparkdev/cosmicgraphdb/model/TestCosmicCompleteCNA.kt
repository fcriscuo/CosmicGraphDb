package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicCompleteCNA {
    fun parseCNAFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE// limit the number of records processed
        deleteCNANodes()
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicCompleteCNA.parseValueMap(it) }
            .forEach { cna ->
                println(
                    "Loading CosmicCNA Id= ${cna.cnvId}  " +
                            " Tumor Id = ${cna.tumorId} " +
                            " Gene: ${cna.geneId}  ${cna.geneSymbol} " +
                            " Sample Id: ${cna.sampleId} \n"

                )
                Neo4jConnectionService.executeCypherCommand(cna.generateCompleteCNACypher())
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cna: CosmicCNA) RETURN COUNT(cna)").toInt()
    }
    private fun deleteCNANodes() =
        Neo4jConnectionService.executeCypherCommand("MATCH (cna: CosmicCNA) DETACH DELETE (cna)")
}

fun main() {
    val cosmicCNAFile =  ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicCompleteCNA.tsv")
    println("Processing COSMIC CNA file $cosmicCNAFile")
    val recordCount = TestCosmicCompleteCNA().parseCNAFile(cosmicCNAFile)
    println("Record count = $recordCount")
}