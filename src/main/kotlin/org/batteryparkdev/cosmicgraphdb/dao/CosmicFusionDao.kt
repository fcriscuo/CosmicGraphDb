package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicFusion
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.common.parseOnPipe
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipProperty
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

class CosmicFusionDao  (private val cosmicFusion: CosmicFusion){
    
     fun generateLoadCosmicModelCypher(): String {
        val cypher = when (Neo4jUtils.nodeExistsPredicate(cosmicFusion.getNodeIdentifier())) {
            false -> generateMergeCypher()
                .plus(" RETURN  ${CosmicFusion.nodename} \n")
            true -> addSecondFusionTypeLabelCypher()
                .plus(" RETURN  ${CosmicFusion.nodename} \n")
        }
        return cypher
    }

    private fun addSecondFusionTypeLabelCypher(): String =
        "CALL apoc.merge.node([\"CosmicFusion\"], {fusion_id: ${cosmicFusion.fusionId}}, " +
                "{},{ last_mod: datetime()}) YIELD node AS ${CosmicFusion.nodename}  \n"
                    .plus("CALL apoc.create.addLabels(${CosmicFusion.nodename}," +
                            " [${cosmicFusion.fusionType.formatNeo4jPropertyValue()}]) " +
                            " YIELD node AS label2 \n")

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"CosmicFusion\", " +
                "${cosmicFusion.fusionType.formatNeo4jPropertyValue()}], " +  // secondary label
                "{fusion_id: ${cosmicFusion.fusionId}, " +
                "{translocation_name: ${cosmicFusion.translocationName.formatNeo4jPropertyValue()} ," +
                " five_prime_chromosome: ${cosmicFusion.five_chromosome}, " +
                " five_prime_strand: ${cosmicFusion.five_strand.formatNeo4jPropertyValue()}," +
                " five_prime_gene_id: ${cosmicFusion.five_geneId}, " +
                " five_prime_gene_symbol: ${cosmicFusion.five_geneSymbol.formatNeo4jPropertyValue()}, " +
                " five_prime_last_observed_exon: ${cosmicFusion.five_lastObservedExon}, " +
                " five_prime_genome_start_from: ${cosmicFusion.five_genomeStartFrom}, " +
                " five_prime_genome_start_to: ${cosmicFusion.five_genomeStartTo}, " +
                " five_prime_genome_stop_from: ${cosmicFusion.five_genomeStopFrom}, " +
                " five_prime_genome_stop_to: ${cosmicFusion.five_genomeStopTo}, " +
                " three_prime_chromosome: ${cosmicFusion.three_chromosome}, " +
                " three_prime_strand: ${cosmicFusion.three_strand.formatNeo4jPropertyValue()}," +
                " three_prime_gene_id: ${cosmicFusion.three_geneId}, " +
                " three_prime_gene_symbol: ${cosmicFusion.three_geneSymbol.formatNeo4jPropertyValue()}, " +
                " three_prime_first_observed_exon: ${cosmicFusion.three_firstObservedExon}, " +
                " three_prime_genome_start_from: ${cosmicFusion.three_genomeStartFrom}, " +
                " three_prime_genome_start_to: ${cosmicFusion.three_genomeStartTo}, " +
                " three_prime_genome_stop_from: ${cosmicFusion.three_genomeStopFrom}, " +
                " three_prime_genome_stop_to: ${cosmicFusion.three_genomeStopTo}, " +
                " pubmed_id: ${cosmicFusion.pubmedId}, created: datetime()}, " +
                "  { last_mod: datetime()}) YIELD node AS ${CosmicFusion.nodename} \n"
    
    companion object: CoreModelDao {
          
        private fun createRelationshipToGeneMutationCollection( model: CoreModel, geneSymbol: String){
            val gene = NodeIdentifier("GeneMutationCollection","gene_symbol", geneSymbol)
            NodeIdentifierDao.defineRelationship(
                RelationshipDefinition( gene, model.getNodeIdentifier(),
                    "HAS_MUTATION", RelationshipProperty("type",
                        model.getNodeIdentifier().primaryLabel)))
        }

        private fun completeMutationRelationships(model: CoreModel){
            // there are two genes involved
            model.getModelGeneSymbol().parseOnPipe().forEach {
                    gene ->  createRelationshipToGeneMutationCollection(model, gene)
            }
            // SampleMutationCollection
            completeRelationshipToSampleMutationCollection(model)
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit =
            CosmicFusionDao.Companion::completeMutationRelationships
    }
   
}