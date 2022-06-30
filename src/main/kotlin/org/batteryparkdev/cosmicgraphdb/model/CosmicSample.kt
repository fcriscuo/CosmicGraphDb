package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicSample(
    val sampleId: Int,
    val sampleName: String,
    val tumorId: Int,
    val primarySite: String,
    val primaryHistology: String,
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
    val germlineMutation: String,
    val nciCode: String,
    val sampleType: String,
    val cosmicPhenotypeId: String,
    val cosmicPatient: CosmicPatient,
    val cosmicTumor: CosmicTumor
): CosmicModel {

     override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicSample", "sample_id", sampleId.toString())

    override fun isValid(): Boolean = sampleId > 0 && tumorId > 0
    override fun getPubMedId(): Int = 0

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(
                CosmicClassification.generateChildRelationshipCypher(
                    cosmicPhenotypeId,
                    nodename
                )
            )
            .plus(cosmicTumor.generateLoadCosmicModelCypher())
            .plus(generateSampleMutationCollectionCypher())
            .plus(" RETURN $nodename\n")


    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node( [\"CosmicSample\"], " +
                " {sample_id: $sampleId}, " +
                " {sample_name: ${Neo4jUtils.formatPropertyValue(sampleName)}, " +
                " tumor_id: $tumorId, " +
                " primary_site: ${Neo4jUtils.formatPropertyValue(primarySite)}," +
                " primary_histology: ${Neo4jUtils.formatPropertyValue(primaryHistology)}," +
                " therapy_relationship: ${Neo4jUtils.formatPropertyValue(therapyRelationship)}," +
                " sample_differentiator: ${Neo4jUtils.formatPropertyValue(therapyRelationship)}, " +
                " mutation_allele_specfication: ${Neo4jUtils.formatPropertyValue(mutationAlleleSpecification)}, " +
                " msi: ${Neo4jUtils.formatPropertyValue(msi)}, average_ploidy: " +
                " ${Neo4jUtils.formatPropertyValue(averagePloidy)}, whole_genome_screen: $wholeGeneomeScreen, " +
                " whole_exome_screen: $wholeExomeScreen, sample_remark: ${Neo4jUtils.formatPropertyValue(sampleRemark)}, " +
                " drug_respose: ${Neo4jUtils.formatPropertyValue(drugResponse)}, " +
                " grade: ${Neo4jUtils.formatPropertyValue(grade)}, " +
                "age_at_tumor_recurrance: $ageAtTumorRecurrence, " +
                " stage: ${Neo4jUtils.formatPropertyValue(stage)}, cytogenetics: " +
                " ${Neo4jUtils.formatPropertyValue(cytogenetics)}, metastatic_site: " +
                " ${Neo4jUtils.formatPropertyValue(metastaticSite)}, germline_mutation: " +
                " ${Neo4jUtils.formatPropertyValue(germlineMutation)}, " +
                " nci_code: ${Neo4jUtils.formatPropertyValue(nciCode)}, sample_type: " +
                " ${Neo4jUtils.formatPropertyValue(sampleType)}, cosmic_phenotype_id: " +
                " ${Neo4jUtils.formatPropertyValue(cosmicPhenotypeId)}," +
                " created: datetime()}) YIELD node as $nodename \n"


    private fun generateSampleMutationCollectionCypher():String =
        "CALL apoc.merge.node( [\"SampleMutationCollection\"], " +
                "{sample_id: $sampleId}," +
                "{created: datetime()},{}) YIELD node as $mutCollNodename \n" +
                "CALL apoc.merge.relationship( ${CosmicSample.nodename}, \"HAS_MUTATION_COLLECTION\" ," +
                " {}, {created: datetime()}, $mutCollNodename, {}) YIELD rel AS mut_coll_rel \n"


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
        const val mutCollNodename = "sample_mut_coll"
        const val pubCollNodename = "sample_pub_coll"
        private const val classificationPrefix = "COSO"  // the classification file uses a prefix, the sample file does not

        fun parseCSVRecord(record: CSVRecord): CosmicSample =
            CosmicSample(
                record.get("sample_id").toInt(),
                record.get("sample_name"),
                record.get("id_tumour").toInt(),
                record.get("primary_site"),
                record.get("primary_histology"),
                record.get("therapy_relationship"),
                record.get("sample_differentiator"),
                record.get("mutation_allele_specification"),
                record.get("msi"), record.get("average_ploidy"),
                convertYNtoBoolean(record.get("whole_genome_screen")),
                convertYNtoBoolean(record.get("whole_exome_screen")),
                removeInternalQuotes(record.get("sample_remark")),
                record.get("drug_response"),
                record.get("grade"),
                parseValidIntegerFromString(record.get("age_at_tumour_recurrence")),
                record.get("stage"),
                record.get("cytogenetics"),
                record.get("metastatic_site"),
                record.get("germline_mutation"),
                record.get("nci_code"),
                record.get("sample_type"),
                classificationPrefix.plus(record.get("cosmic_phenotype_id")),
                CosmicPatient.parseCSVRecord(record),
                CosmicTumor.parseCSVRecord(record)
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