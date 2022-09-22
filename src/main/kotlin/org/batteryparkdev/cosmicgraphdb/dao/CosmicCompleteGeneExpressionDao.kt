package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteGeneExpression
import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

class CosmicCompleteGeneExpressionDao(private val completeExpression: CosmicCompleteGeneExpression) {

    fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(" RETURN ${CosmicCompleteGeneExpression.nodename}")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CompleteGeneExpression\"], " +
            "  {key: ${completeExpression.key}}, " +
            "  {regulation: ${completeExpression.regulation.formatNeo4jPropertyValue()}, " +
            "  gene_symbol: ${completeExpression.geneSymbol.formatNeo4jPropertyValue()} ," +
            "  sample_name: ${completeExpression.sampleName.formatNeo4jPropertyValue()} ," +
            "  sample_id: ${completeExpression.sampleId}, " +
            " z_score: ${completeExpression.zScore}, " +
            "study_id: ${completeExpression.studyId}," +
            " created: datetime()}, " +
            " { last_mod: datetime()}) YIELD node AS ${CosmicCompleteGeneExpression.nodename} \n"

    companion object : org.batteryparkdev.genomicgraphcore.common.CoreModelDao {

        private fun completeCodingMutationRelationships(model: CoreModel) {
            completeRelationshipToGene(model)
            completeRelationshipToSample(model)
        }

        override val modelRelationshipFunctions: (CoreModel) -> kotlin.Unit =
            ::completeCodingMutationRelationships

        fun completeRelationshipToSample(model: CoreModel) {
            val sample = NodeIdentifier(
                "CosmicSample", "sample_id",
                model.getModelSampleId()
            )
            NodeIdentifierDao.defineRelationship(
                RelationshipDefinition(
                    sample, model.getNodeIdentifier(),
                    "HAS_EXPRESSION"
                )
            )
        }

        fun completeRelationshipToGene(model: CoreModel) {
            val gene = CosmicGeneCensus.generateNodeIdentifierByValue(model.getModelGeneSymbol())
            NodeIdentifierDao.defineRelationship(
                RelationshipDefinition(
                    model.getNodeIdentifier(), gene,
                    "HAS_GENE"
                )
            )
        }
    }
}
