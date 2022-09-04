package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicBreakpoint
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipProperty

class CosmicBreakpointDao(private val cosmicBreakpoint: CosmicBreakpoint) {

    fun generateCosmicBreakpointCypher(): String = generateMergeCypher()
        .plus(" RETURN ${CosmicBreakpoint.nodename}\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicBreakpoint\",\"Mutation\"], " +
            " {mutation_id: ${cosmicBreakpoint.mutationId}, " +
            " {sample_id: ${cosmicBreakpoint.sampleId} ," +
            " sample_name: ${cosmicBreakpoint.sampleName.formatNeo4jPropertyValue()}, " +
            " chromosome_from: \"${cosmicBreakpoint.chromosomeFrom}\" , " +
            " location_from_min: ${cosmicBreakpoint.locationFromMin}," +
            " location_from_max: ${cosmicBreakpoint.locationFromMax}, " +
            " strand_from: ${cosmicBreakpoint.strandFrom.formatNeo4jPropertyValue()}, " +
            " chromosome_to: \"${cosmicBreakpoint.chromosomeTo}\", " +  // can't use utility here
            " location_to_min: ${cosmicBreakpoint.locationToMin}," +
            " location_to_max: ${cosmicBreakpoint.locationToMax}, " +
            " strand_to: ${cosmicBreakpoint.strandTo.formatNeo4jPropertyValue()}," +
            "  pubmed_id: ${cosmicBreakpoint.pubmedId}, " +
            " study_id: ${cosmicBreakpoint.studyId.toString()}," +
            " created: datetime() }," +
            " { last_mod: datetime()}) YIELD node AS ${CosmicBreakpoint.nodename} \n "

    companion object : CoreModelDao {
        final val nodename = "breakpoint"

        override val modelRelationshipFunctions: (CoreModel) -> Unit
             = ::completeBreakpointRelationships
        
        fun completeBreakpointRelationships(model: CoreModel) {
            completeRelationshipToSampleMutationCollection(model)
            completeRelationshipToCosmicStruct(model)
        }

        /*
        Complete relationship to CosmicStruct node
         */
        private fun completeRelationshipToCosmicStruct(model:CoreModel) {
            val struct = NodeIdentifier("CosmicStruct", "mutation_id",
            model.getNodeIdentifier().idValue)
            NodeIdentifierDao.defineRelationship(
                RelationshipDefinition(struct, model.getNodeIdentifier(), "HAS_BREAKPOINT"))
        }


    }
}