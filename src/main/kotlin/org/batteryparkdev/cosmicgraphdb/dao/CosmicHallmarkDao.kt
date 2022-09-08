package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicHallmark
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

class CosmicHallmarkDao (private val hallmark: CosmicHallmark) {

    fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(generateMergeHallmarkCollectionCypher())
            //.plus(generateHasHallmarkRelationshipCypher())
            .plus(" RETURN ${CosmicHallmark.nodename}")

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node( [\"CosmicHallmark\"]," +
                " {hallmark_id:  ${hallmark.hallmarkId} , " +
                "  {gene_symbol: ${hallmark.geneSymbol.formatNeo4jPropertyValue()}, " +
                "  cell_type: ${hallmark.cellType.formatNeo4jPropertyValue()}, " +
                "  pubmed_id: ${hallmark.pubmedId}, " +
                "  hallmark: ${hallmark.hallmark.formatNeo4jPropertyValue()}, " +
                "  impact: ${hallmark.impact.formatNeo4jPropertyValue()}, " +
                "  description: ${hallmark.description.formatNeo4jPropertyValue()}, " +
                "  created: datetime()}) YIELD node as ${CosmicHallmark.nodename} \n"

    private fun generateMergeHallmarkCollectionCypher(): String =
        " CALL apoc.merge.node( [\"CosmicHallmarkCollection\"], " +
                "{ gene_symbol: ${hallmark.geneSymbol.formatNeo4jPropertyValue()}}," +
                "  {created: datetime()}," +
                " {lastSeen: datetime()} ) YIELD node as ${CosmicHallmark.collectionname} \n "

    companion object: CoreModelDao {
        private fun completeHallmarkRelationships( model: CoreModel) {
            generateHallmarkToHallmarkCollectionRelationship(model)
            generateCosmicGeneToHallmarkCollectionRelationship(model)
        }

     private  fun generateHallmarkToHallmarkCollectionRelationship(model: CoreModel) {
           val  hallmarkCollectionNode = NodeIdentifier("CosmicHallmarkCollection", "gene_symbol",
           model.getModelGeneSymbol())
            NodeIdentifierDao.defineRelationship(RelationshipDefinition(hallmarkCollectionNode, model.getNodeIdentifier(),
           "HAS_HALLMARK" ))
        }

        private fun generateCosmicGeneToHallmarkCollectionRelationship(model:CoreModel){
            val  hallmarkCollection= NodeIdentifier("CosmicHallmarkCollection", "gene_symbol",
                model.getModelGeneSymbol())
            val gene = NodeIdentifier("CosmicGene","gene_symbol", model.getModelGeneSymbol())
            NodeIdentifierDao.defineRelationship(
                RelationshipDefinition(gene,hallmarkCollection,
             "HAS_HALLMARK_COLLECTION")
            )
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit =
            ::completeHallmarkRelationships
    }
}