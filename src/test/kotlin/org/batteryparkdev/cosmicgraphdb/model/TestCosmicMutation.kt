package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicMutation {
    fun parseCosmicMutationFile(filename: String): Int {
        val LIMIT = Long.MAX_VALUE
        // limit the number of records processed
       deleteExistingNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicMutation.parseValueMap(it) }
            // only process COSMIC census genes
            .filter { mutation ->
                Neo4jUtils.nodeExistsPredicate(
                    NodeIdentifier(
                        "CosmicGene", "gene_symbol",
                        mutation.geneSymbol
                    )
                )
            }
            .forEach { mutation ->
                println("Loading mutation ${mutation.mutationId} for gene: ${mutation.geneSymbol}")
                Neo4jConnectionService.executeCypherCommand(mutation.generateCosmicMutationCypher())
                // create a Publication node if a PubMed id is present
                mutation.createPubMedRelationship(mutation.pubmedId)
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cm:CosmicMutation) RETURN COUNT(cm)").toInt()
    }
    private fun deleteExistingNodes(){
        Neo4jUtils.detachAndDeleteNodesByName("CosmicTumor")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicMutation")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicSample")
    }

}

fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicMutantExportCensus.tsv")
    val recordCount =
        TestCosmicMutation().parseCosmicMutationFile(filename)
    println("Mutation record count = $recordCount")
}
