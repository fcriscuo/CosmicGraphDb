package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicCodingMutation
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

class CosmicCodingMutationDao ( private val codingMutation: CosmicCodingMutation) {


    fun generateLoadCosmicModelCypher(): String =
        mergeCodingMutationCypher
            //.plus(generateGeneMutationCollectionRelationshipCypher(geneSymbol, CosmicCodingMutation.nodename))
           // .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, CosmicCodingMutation.nodename))
            .plus(" RETURN ${CosmicCodingMutation.nodename}")

    private val mergeCodingMutationCypher = " CALL apoc.merge.node( [\"CosmicCodingMutation\",\"Mutation\"], " +
            " {mutation_id: $codingMutation.mutationId}, " + // key
            " { legacy_mutation_id: ${codingMutation.legacyMutationId.formatNeo4jPropertyValue()} ," +
            " gene_symbol: ${codingMutation.geneSymbol.formatNeo4jPropertyValue()}, " +
            "  gene_cds_length: $codingMutation.geneCDSLength, " +
            " genomic_mutation_id: ${codingMutation.genomicMutationId.formatNeo4jPropertyValue()} ,"+
            " mutation_cds: ${codingMutation.mutationCds.formatNeo4jPropertyValue()}," +
            " mutation_aa: ${codingMutation.mutationAA.formatNeo4jPropertyValue()}, " +
            " description: ${codingMutation.mutationDescription.formatNeo4jPropertyValue()}," +
            " zygosity: ${codingMutation.mutationZygosity.formatNeo4jPropertyValue()}, " +
            " loh: ${codingMutation.LOH.formatNeo4jPropertyValue()}, " +
            " grch: ${codingMutation.GRCh.formatNeo4jPropertyValue()}, " +
            " genome_position: ${codingMutation.mutationGenomePosition.formatNeo4jPropertyValue()}, " +
            " strand: ${codingMutation.mutationStrand.formatNeo4jPropertyValue()}, " +
            " resistance_mutation: ${codingMutation.resistanceMutation.formatNeo4jPropertyValue()}, " +
            " somatic_status: $codingMutation.{mutationSomaticStatus.formatNeo4jPropertyValue()}, " +
            " pubmed_id: $codingMutation.pubmedId, " +
            " genome_wide_screen: $codingMutation.genomeWideScreen, " +
            " hgvsp: ${codingMutation.hgvsp.formatNeo4jPropertyValue()}, " +
            " hgvsc: ${codingMutation.hgvsc.formatNeo4jPropertyValue()}, " +
            " hgvsq: ${codingMutation.hgvsg.formatNeo4jPropertyValue()}, " +
            " tier: ${codingMutation.tier.formatNeo4jPropertyValue()}, " +
            "  created: datetime()},{}) YIELD node as ${CosmicCodingMutation.nodename} \n"

    companion object: CoreModelDao {
      private fun completeCodingMutationRelationships(model: CoreModel) {
          completeRelationshipToGeneMutationCollection(model)
          completeRelationshipToSampleMutationCollection(model)
      }

        override val modelRelationshipFunctions: (CoreModel) -> Unit =
            ::completeCodingMutationRelationships

    }
}