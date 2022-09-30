package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicNCV
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

class CosmicNCVDao(private val ncv: CosmicNCV) {

    fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
        .plus(" RETURN  ${CosmicNCV.nodename}")

    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node( [\"CosmicNCV\"], " +
                "{genomic_mutation_id: ${ncv.genomicMutationId.formatNeo4jPropertyValue()}}," +  // unique value for node
                " { sample_name: ${ncv.sampleName.formatNeo4jPropertyValue()}, " +
                " sample_id: ${ncv.sampleId}, " +
                "tumor_id: ${ncv.tumorId}, " +
                " legacy_mutation_id: ${ncv.legacyMutationId.formatNeo4jPropertyValue()}, " +
                " zygosity: ${ncv.zygosity.formatNeo4jPropertyValue()}, " +
                " grch: ${ncv.grch}, " +
                "genome_position: ${ncv.genomePosition.formatNeo4jPropertyValue()}, " +
                " mutation_somatic_status: ${ncv.mutationSomaticStatus.formatNeo4jPropertyValue()}, " +
                " wt_seq: ${ncv.wtSeq.formatNeo4jPropertyValue()}," +
                " mut_seq: ${ncv.mutSeq.formatNeo4jPropertyValue()}, " +
                " whole_genome_reseq: ${ncv.wholeGenomeReseq}, " +
                "whole_exome: ${ncv.wholeExome}, " +
                "study_id: ${ncv.studyId}, " +
                " pubmed_id: ${ncv.pubmedId}," +
                " hgvsg: ${ncv.hgvsg.formatNeo4jPropertyValue()} ," +
                " created: datetime() }, { last_mod: datetime()}) YIELD node AS ${CosmicNCV.nodename} \n "

    companion object : CoreModelDao {

        private fun completeNCVRelationships(model: CoreModel) {
            completeRelationshipToSampleMutationCollection(model)
            createPubMedRelationships(model)
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit
            = ::completeNCVRelationships
    }
}