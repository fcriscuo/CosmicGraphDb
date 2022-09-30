package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteCNA
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

class CosmicCompleteCNADao(private val completeCNA: CosmicCompleteCNA) {

    fun generateLoadCosmicCompleteCNACypher(): String =
        generateMergeCypher()
            .plus(" RETURN ${CosmicCompleteCNA.nodename}")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicCompleteCNA\"], " +
            " {cna_id: ${completeCNA.cnaId.formatNeo4jPropertyValue()} }, " +
            " { cnv_id: ${completeCNA.cnvId}, " +
            "  tumor_id: $completeCNA,tumorId, " +
            " total_cn: ${completeCNA.totalCn}, " +
            "minor_allele: ${completeCNA.minorAllele}," +
            " study_id: ${completeCNA.studyId}," +
            " grch: ${completeCNA.grch.formatNeo4jPropertyValue()}," +
            " chromosome_start_stop: ${completeCNA.chromosomeStartStop}," +
            "created: datetime()  " +
            " }, { last_mod: datetime()}) YIELD node AS ${CosmicCompleteCNA.nodename}\n"

    companion object : org.batteryparkdev.genomicgraphcore.common.CoreModelDao {

        private fun completeCodingMutationRelationships(model: org.batteryparkdev.genomicgraphcore.common.CoreModel) {
            completeRelationshipToGeneMutationCollection(model)
            completeRelationshipToSampleMutationCollection(model)
        }

        override val modelRelationshipFunctions: (org.batteryparkdev.genomicgraphcore.common.CoreModel) -> kotlin.Unit =
            ::completeCodingMutationRelationships
    }
}