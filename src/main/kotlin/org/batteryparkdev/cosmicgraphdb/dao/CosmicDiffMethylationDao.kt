package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicDiffMethylation
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

class CosmicDiffMethylationDao ( private val diffMethylation: CosmicDiffMethylation){

     fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(" RETURN ${CosmicDiffMethylation.nodename}")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicDiffMethylation\"], " +
            " { key: apoc.create.uuid()}," +
            "{ study_id: ${diffMethylation.studyId}, " +
            " tumor_id: ${diffMethylation.tumorId}," +
            " sample_id: ${diffMethylation.sampleId}, " +
            " fragment_id: ${diffMethylation.fragmentId.formatNeo4jPropertyValue()} ," +
            " genome_version: ${diffMethylation.genomeVersion.formatNeo4jPropertyValue()}, " +
            " ,chromosome: ${diffMethylation.chromosome}," +
            " position: ${diffMethylation.position}, " +
            " strand: ${diffMethylation.strand.formatNeo4jPropertyValue()}," +
            " gene_name: ${diffMethylation.geneName.formatNeo4jPropertyValue()} , " +
            " methylation: ${diffMethylation.methylation.formatNeo4jPropertyValue()}," +
            " avg_beta_value_normal: ${diffMethylation.avgBetaValueNormal} , " +
            " beta_value: ${diffMethylation.betaValue}," +
            " two_sided_p_value: ${diffMethylation.twoSidedPValue}, " +
            " created: datetime()}, " +
            " { last_mod: datetime()}) YIELD node AS ${CosmicDiffMethylation.nodename} \n"


    companion object : org.batteryparkdev.genomicgraphcore.common.CoreModelDao {

        private fun completeMutationRelationships(model: org.batteryparkdev.genomicgraphcore.common.CoreModel) {
            completeRelationshipToSampleMutationCollection(model)
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit
             = ::completeMutationRelationships

    }
}