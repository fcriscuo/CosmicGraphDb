package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicClassification
import org.batteryparkdev.cosmicgraphdb.model.CosmicSample
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

class CosmicSampleDao(private val sample: CosmicSample) {
    // CosmicPatient -> CosmicTumor -> CosmicSample -> SampleMutationCollection

    fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
        .plus(generateSampleMutationCollectionCypher())
        .plus(sample.cosmicTumor.generateLoadModelCypher())
        .plus(" RETURN  ${CosmicSample.nodename}")

    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node( [\"CosmicSample\",\"Sample\"] " +
                " {sample_id: ${sample.sampleId} }, " +
                " {sample_name: ${sample.sampleName.formatNeo4jPropertyValue()}, " +
                " tumor_id: ${sample.tumorId}, " +
                " primary_site: ${sample.primarySite.formatNeo4jPropertyValue()}," +
                " primary_histology: ${sample.primaryHistology.formatNeo4jPropertyValue()}," +
                " therapy_relationship: ${sample.therapyRelationship.formatNeo4jPropertyValue()}," +
                " sample_differentiator: ${sample.therapyRelationship.formatNeo4jPropertyValue()}, " +
                " mutation_allele_specfication: ${sample.mutationAlleleSpecification.formatNeo4jPropertyValue()}, " +
                " msi: ${sample.msi.formatNeo4jPropertyValue()}, " +
                " average_ploidy: " +
                " ${sample.averagePloidy.formatNeo4jPropertyValue()}, " +
                " whole_genome_screen: ${sample.wholeGeneomeScreen}, " +
                " whole_exome_screen: ${sample.wholeExomeScreen}, " +
                "sample_remark: ${sample.sampleRemark.formatNeo4jPropertyValue()}, " +
                " drug_respose: ${sample.drugResponse.formatNeo4jPropertyValue()}, " +
                " grade: ${sample.grade.formatNeo4jPropertyValue()}, " +
                "age_at_tumor_recurrance: ${sample.ageAtTumorRecurrence}, " +
                " stage: ${sample.stage.formatNeo4jPropertyValue()}, cytogenetics: " +
                " ${sample.cytogenetics.formatNeo4jPropertyValue()}, metastatic_site: " +
                " ${sample.metastaticSite.formatNeo4jPropertyValue()}, germline_mutation: " +
                " ${sample.germlineMutation.formatNeo4jPropertyValue()}, " +
                " nci_code: ${sample.nciCode.formatNeo4jPropertyValue()}, sample_type: " +
                " ${sample.sampleType.formatNeo4jPropertyValue()}, cosmic_phenotype_id: " +
                " ${sample.cosmicPhenotypeId.formatNeo4jPropertyValue()}," +
                " created: datetime()}) YIELD node as ${CosmicSample.nodename} \n"

    private fun generateSampleMutationCollectionCypher():String =
        "CALL apoc.merge.node( [\"SampleMutationCollection\"], " +
                "{sample_id: ${sample.sampleId} }," +
                "{created: datetime()},{}) YIELD node as ${CosmicSample.mutCollNodename} \n"

    companion object : CoreModelDao {
       private fun createCosmicSampleRelationships(model: CoreModel){
           createClassificationRelationship(model)
           createMutationCollectionRelationship(model)
       }

        private fun createClassificationRelationship(model: CoreModel) {
            if (model is CosmicSample){
                val classification = CosmicClassification.generateNodeIdentifierByValue(model.cosmicPhenotypeId)
                NodeIdentifierDao.defineRelationship(RelationshipDefinition(model.getNodeIdentifier(), classification,
                "HAS_CLASSIFICATION"))
            }
        }

        private fun createMutationCollectionRelationship(model: CoreModel){
            val collectionNode = NodeIdentifier("SampleMutationCollection", "sample_id",
                model.getModelSampleId())
            NodeIdentifierDao.defineRelationship(
                RelationshipDefinition(model.getNodeIdentifier(),
            collectionNode,"HAS_MUTATION_COLECTION")
            )
        }

        override val modelRelationshipFunctions: (CoreModel) -> Unit
          = ::createCosmicSampleRelationships
    }
}