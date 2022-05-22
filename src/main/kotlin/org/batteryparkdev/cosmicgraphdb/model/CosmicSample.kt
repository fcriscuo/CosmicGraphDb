package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicSample(
    val sampleId: Int,
    val sampleName: String,
    val tumorId: Int,
    val therapyRelationship: String,
    val sampleDifferentiator: String,
    val mutationAlleleSpecification: String,
    val msi: String,
    val averagePloidy: String,
    val wholeGeneomeScreen: Boolean,
    val wholeExomeScreen: Boolean,
    val sampleRemark: String,
    val drugResponse: String,
    val grade: String,
    val ageAtTumorRecurrence: Int,
    val stage: String,
    val cytogenetics: String,
    val metastaticSite: String,
    val tumorSource: String,
    val tumorRemark: String,
    val germlineMutation: String,
    val nciCode: String,
    val sampleType: String,
    val cosmicPhenotypeId: String,
    val cosmicPatient: CosmicPatient

) {

    fun generateCosmicSampleCypher(): String =
        generateMergeCypher()
            .plus(
                CosmicClassification.generateChildRelationshipCypher(
                    cosmicPhenotypeId,
                    nodename
                )
            )
            .plus(generateTumorRelationshipCypher())
            .plus(cosmicPatient.generateCosmicPatientCypher())
            .plus(" RETURN $nodename\n")

    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node( [\"CosmicSample\"], " +
                " {sample_id: $sampleId}, " +
                " {sample_name: ${Neo4jUtils.formatPropertyValue(sampleName)}, " +
                " tumor_id: $tumorId, " +
                " therapy_relationship: ${Neo4jUtils.formatPropertyValue(therapyRelationship)}," +
                " sample_differentiator: ${Neo4jUtils.formatPropertyValue(therapyRelationship)}, " +
                " mutation_allele_specfication: ${Neo4jUtils.formatPropertyValue(mutationAlleleSpecification)}, " +
                " msi: ${Neo4jUtils.formatPropertyValue(msi)}, average_ploidy: " +
                " ${Neo4jUtils.formatPropertyValue(averagePloidy)}, whole_genome_screen: $wholeGeneomeScreen, " +
                " whole_exome_screen: $wholeExomeScreen, sample_remark: ${Neo4jUtils.formatPropertyValue(sampleRemark)}, " +
                " drug_respose: ${Neo4jUtils.formatPropertyValue(drugResponse)}, " +
                " grade: ${Neo4jUtils.formatPropertyValue(grade)}, age_at_tumor_recurrance: $ageAtTumorRecurrence, " +
                " stage: ${Neo4jUtils.formatPropertyValue(stage)}, cytogenetics: " +
                " ${Neo4jUtils.formatPropertyValue(cytogenetics)}, metastatic_site: " +
                " ${Neo4jUtils.formatPropertyValue(metastaticSite)}, tumor_source: " +
                " ${Neo4jUtils.formatPropertyValue(tumorSource)}, tumor_remark: " +
                " ${Neo4jUtils.formatPropertyValue(germlineMutation)}, " +
                " nci_code: ${Neo4jUtils.formatPropertyValue(nciCode)}, sample_type: " +
                " ${Neo4jUtils.formatPropertyValue(sampleType)}, cosmic_phenotype_id: " +
                " ${Neo4jUtils.formatPropertyValue(cosmicPhenotypeId)}," +
                " created: datetime()}) YIELD node as $nodename \n"

    /*
    Private function to create a CosmicTumor - [HAS_SAMPLE] -> CosmicSample relationship
    Will create a placeholder Tumor node if tumorid is novel
     */
    private fun generateTumorRelationshipCypher():String {
        val relationship = "HAS_SAMPLE"
        val relname = "rel_sample"
        return generateTumorMatchCypher()
            .plus("CALL apoc.merge.relationship(${CosmicTumor.nodename}, '$relationship', "  +
                " {}, {created: datetime()}, " +
                " $nodename, {} ) YIELD rel AS $relname \n" )
    }

     private  fun generateTumorMatchCypher(): String =
       "CALL apoc.merge.node ([\"CosmicTumor\"],{tumor_id: $tumorId},{} ) " +
               " YIELD node AS tumor\n"

    companion object : AbstractModel {
        const val nodename = "sample"
        private const val classificationPrefix = "COSO"  // the classification file uses a prefix, the sample file does not

        // COSO36736185  vs  36736185
        fun parseValueMap(value: Value): CosmicSample =
            CosmicSample(
                value["sample_id"].asString().toInt(),
                value["sample_name"].asString(),
                value["id_tumour"].asString().toInt(),
                value["therapy_relationship"].asString(),
                value["sample_differentiator"].asString(),
                value["mutation_allele_specification"].asString(),
                value["msi"].asString(), value["average_ploidy"].asString(),
                convertYNtoBoolean(value["whole_genome_screen"].asString()),
                convertYNtoBoolean(value["whole_exome_screen"].asString()),
                removeInternalQuotes(value["sample_remark"].asString()),
                value["drug_response"].asString(),
                value["grade"].asString(),
                parseValidIntegerFromString(value["age_at_tumour_recurrence"].asString()),
                value["stage"].asString(), value["cytogenetics"].asString(),
                value["metastatic_site"].asString(),
                value["tumour_source"].asString(),
                removeInternalQuotes(value["tumour_remark"].asString()),
                value["germline_mutation"].asString(),
                value["nci_code"].asString(),
                value["sample_type"].asString(),
                classificationPrefix.plus(value["cosmic_phenotype_id"].asString()),
                CosmicPatient.parseValueMap(value)
            )


        fun generateMatchCosmicSampleCypher(sampleId: Int) =
            "CALL apoc.merge.node ([\"CosmicSample\"],{sample_id: $sampleId},{created: datetime()},{} )" +
                    " YIELD node AS $nodename\n"

        fun generateChildRelationshipCypher(sampleId: Int, childLabel: String): String {
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_sample"
            return generateMatchCosmicSampleCypher(sampleId).plus(
                "CALL apoc.merge.relationship( $nodename, '$relationship', " +
                        " {}, {created: datetime()}, $childLabel,{} )" +
                        " YIELD rel AS $relname \n"
            )
        }
    }
}