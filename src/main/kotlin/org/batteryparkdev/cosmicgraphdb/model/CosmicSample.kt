package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicSampleDao
import org.batteryparkdev.genomicgraphcore.common.*
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

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
    val cosmicTumor: CoreModel
): CoreModel {
    override fun createModelRelationships() = CosmicSampleDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String = CosmicSampleDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = ""

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicSample", "sample_id", sampleId.toString())

    override fun getPubMedIds(): List<Int> = emptyList()

    override fun isValid(): Boolean = sampleId > 0 && tumorId > 0

    companion object : CoreModelCreator {
        const val nodename = "sample"
        const val mutCollNodename = "sample_mut_coll"
        private const val classificationPrefix = "COSO"  // the classification file uses a prefix, the sample file does not

        fun parseCsvRecord(record: CSVRecord): CosmicSample =
            CosmicSample(
                record.get("sample_id").parseValidInteger(),
                record.get("sample_name"),
                record.get("id_tumour").parseValidInteger(),
                record.get("primary_site"),
                record.get("primary_histology"),
                record.get("therapy_relationship"),
                record.get("sample_differentiator"),
                record.get("mutation_allele_specification"),
                record.get("msi"), record.get("average_ploidy"),
                record.get("whole_genome_screen").YNtoBoolean(),
                record.get("whole_exome_screen").YNtoBoolean(),
                record.get("sample_remark").removeInternalQuotes(),
                record.get("drug_response"),
                record.get("grade"),
                record.get("age_at_tumour_recurrence").parseValidInteger(),
                record.get("stage"),
                record.get("cytogenetics"),
                record.get("metastatic_site"),
                record.get("germline_mutation"),
                record.get("nci_code"),
                record.get("sample_type"),
                classificationPrefix.plus(record.get("cosmic_phenotype_id")),
                CosmicTumor.createCoreModelFunction.invoke(record)
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            = ::parseCsvRecord
    }
}