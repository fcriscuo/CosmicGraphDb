package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicStruct
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

class CosmicStructDao (private val struct: CosmicStruct) {

    fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
        .plus(" RETURN  ${CosmicStruct.nodename}")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicStruct\"," +
            // use struct type as a secondary label
            "${struct.structType.formatNeo4jPropertyValue()}, " +
            " \"Mutation\" ], " +
            " {mutation_id: ${struct.mutationId} }, " +
            " {sample_id: ${struct.sampleId}, " +
            " tumor_id: ${struct.tumorId}, " +
            " mutation_type: ${struct.mutationType.formatNeo4jPropertyValue()}, " +
            " description: ${struct.description.formatNeo4jPropertyValue()}, " +
            "  pubmed_id: ${struct.pubmedId}, " +
            "  created: datetime() }, " +
            " { last_mod: datetime()}) YIELD node AS ${CosmicStruct.nodename} \n"

    companion object: CoreModelDao {
        private fun createCosmicStructRelationships(model: CoreModel) {
            completeRelationshipToSampleMutationCollection(model)
            createPubMedRelationships(model)
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit
            = ::createCosmicStructRelationships

    }
}