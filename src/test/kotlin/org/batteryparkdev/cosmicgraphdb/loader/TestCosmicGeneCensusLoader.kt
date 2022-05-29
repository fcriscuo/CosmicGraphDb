package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicGeneCensusLoader {

    fun loadCosmicGeneCensusFile(filename: String): Int {
        deleteCosmicGeneCensusNodes()
        println("Loading CosmicGeneCensus data from file: $filename")
        CosmicGeneCensusLoader.loadCosmicGeneCensusData(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (cg: CosmicGene) RETURN COUNT(cg)").toInt()
    }

    private fun deleteCosmicGeneCensusNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicAnnotation")
        Neo4jUtils.detachAndDeleteNodesByName("GeneMutationCollection")
        Neo4jUtils.detachAndDeleteNodesByName("GenePublicationCollection")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicGeneCensus")
    }
}

fun main () {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("cancer_gene_census.csv")
    println("Loading Cosmic Gene Census data from: $filename")
    val rowCount = TestCosmicGeneCensusLoader().loadCosmicGeneCensusFile(filename)
    println("Loaded CosmicCGeneCensus data row count = $rowCount")
}
