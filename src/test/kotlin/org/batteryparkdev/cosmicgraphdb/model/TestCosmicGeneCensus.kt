package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicGeneCensus {
    private val LIMIT = Long.MAX_VALUE

    /*
    n.b. file name specification must be a full path since it is resolved by the Neo4j server
     */
    fun parseCosmicGeneCensusFile(filename: String): Int {
        // delete existing CosmicGene nodes & annotations
        deleteExistingGeneNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicGeneCensus.parseValueMap(it) }
            .forEach { gene ->
                Neo4jConnectionService.executeCypherCommand(gene.generateCosmicGeneCypher())
                println("Loading cosmic census gene: ${gene.geneSymbol}")

            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cg: CosmicGene) RETURN COUNT(cg)").toInt()
    }

    private fun deleteExistingGeneNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicAnnotation")
        Neo4jUtils.detachAndDeleteNodesByName("GeneMutationCollection")
        Neo4jUtils.detachAndDeleteNodesByName("GenePublicationCollection")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicGene")
    }
}

fun main() {
    val cosmicGeneCensusFile = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("cancer_gene_census.csv")
    val recordCount =
        TestCosmicGeneCensus().parseCosmicGeneCensusFile(cosmicGeneCensusFile)
    println("Processed $cosmicGeneCensusFile  record count = $recordCount")
}