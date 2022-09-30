package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicResistanceMutation
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

class CosmicResistanceMutationCollectionDao ( private val resistMut: CosmicResistanceMutation){

    fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
        .plus(generateMatchDrugCypher())
        .plus(" RETURN  ${CosmicResistanceMutation.nodename}")
    
    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"CosmicResistanceMutation\", \"Mutation\"], " +
                "  { mutation_id: ${resistMut.mutationId} }, " +
                " { gene_symbol: ${resistMut.geneSymbol.formatNeo4jPropertyValue()}, " +
                " genomic_mutation_id: ${resistMut.genomicMutationId.formatNeo4jPropertyValue()}, " +
                " legacy_mutation_id: ${resistMut.legacyMutationId.formatNeo4jPropertyValue()}, " +
                " aa_mutation: ${resistMut.aaMutation.formatNeo4jPropertyValue()}, " +
                " cds_mutation: ${resistMut.cdsMutation.formatNeo4jPropertyValue()}," +
                " transcript: ${resistMut.transcript.formatNeo4jPropertyValue()}, " +
                " somatic_status: ${resistMut.somaticStatus.formatNeo4jPropertyValue()}, " +
                " zygosity: ${resistMut.zygosity.formatNeo4jPropertyValue()}, " +
                " genome_coordinates: ${resistMut.genomeCoordinates.formatNeo4jPropertyValue()}, " +
                " tier: ${resistMut.tier}, " +
                " hgvsp: ${resistMut.hgvsp.formatNeo4jPropertyValue()}, " +
                " hgvsc: ${resistMut.hgvsc.formatNeo4jPropertyValue()}, " +
                " hgvsg: ${resistMut.hgvsg.formatNeo4jPropertyValue()}, " +
                " transcript: ${resistMut.transcript.formatNeo4jPropertyValue()}, " +
                " pubmed_id: ${resistMut.pubmedId}, " +
                "  created: datetime()}) YIELD node as ${CosmicResistanceMutation.nodename} \n"
    
    private fun generateMatchDrugCypher(): String =
        "CALL apoc.merge.node( [\"CosmicDrug\",\"Drug\"], " +
                " {drug_name: ${resistMut.drugName.lowercase().formatNeo4jPropertyValue()}}, " +
                " {created: datetime()},{} )" +
                " YIELD node AS drug_node \n"

    companion object: CoreModelDao{

       private fun completeResistanceMutationRelationships(model: CoreModel) {
           completeRelationshipToSampleMutationCollection(model)
           completeRelationshipToDrug(model)
           createPubMedRelationships(model)
       }
        private fun completeRelationshipToDrug(model: CoreModel) {
            if (model is CosmicResistanceMutation){
                val drugNode = NodeIdentifier("CosmicDrug","drug_name", model.drugName)
                NodeIdentifierDao.defineRelationship(RelationshipDefinition(model.getNodeIdentifier(), drugNode,
                "RESISTANT_TO"))
            }
        }
       
        override val modelRelationshipFunctions: (CoreModel) -> Unit
           = ::completeResistanceMutationRelationships
    }
}