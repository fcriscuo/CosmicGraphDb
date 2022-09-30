package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicSample
import org.batteryparkdev.cosmicgraphdb.model.CosmicTumor
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

class CosmicTumorDao(private val tumor: CosmicTumor) {

    fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
        .plus(tumor.patient.generateLoadModelCypher())
    // Cosmic Tumor is loaded as a part of CosmicSample
    // A Neo4j RETURN clause is not needed here

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node( [\"CosmicTumor\",\"Tumor\"], " +
                "{tumor_id: ${tumor.tumorId} }," +
                " { tumor_source: ${tumor.tumorSource.formatNeo4jPropertyValue()} , " +
                " tumor_remark: ${tumor.tumorRemark.formatNeo4jPropertyValue()} , " +
                "  created: datetime()},{}) YIELD node as ${CosmicTumor.nodename} \n"

    companion object : CoreModelDao {
        // Tumor - HAS_SAMPLE -> Sample
        private fun createRelationshipToSample(model: CoreModel) {
            val sampleNode = CosmicSample.generateNodeIdentifierByValue(model.getModelSampleId())
            NodeIdentifierDao.defineRelationship(RelationshipDefinition(model.getNodeIdentifier(), sampleNode,
            "HAS_SAMPLE"))
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit
           = ::createRelationshipToSample


    }
}