package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicClassificationDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

data class CosmicClassification(
    val cosmicPhenotypeId: String,
    val siteType: CosmicType,
    val histologyType: CosmicType,
    val cosmicSiteType: CosmicType,
    val nciCode: String,
    val efoUrl: String
): CoreModel {
    override val idPropertyValue: String = this.cosmicPhenotypeId

    override fun createModelRelationships() = CosmicClassificationDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String = CosmicClassificationDao(this)
        .generateCosmicClassificationCypher()

    override fun getModelGeneSymbol(): String =""

    override fun getModelSampleId(): String =""

    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(CosmicClassification, this)


    override fun getPubMedIds(): List<Int> = emptyList()

    override fun isValid(): Boolean = cosmicPhenotypeId.isNotEmpty()

    companion object : CoreModelCreator {
        override val nodename = "classification"
        override val nodeIdProperty: String
            get() = "phenotype_id"
        override val nodelabel: String
            get() = "CosmicClassification"

        fun parseCsvRecord(record:CSVRecord): CosmicClassification {
            val nciCode = record.get("NCI_CODE") ?: "NS"
            val efo = record.get("EFO") ?: "NS"
            val phenoId = record.get("COSMIC_PHENOTYPE_ID") ?: "NS"
            return CosmicClassification(
                phenoId,
                resolveSiteType(record),
                resolveHistologyType(record),
                resolveCosmicSiteType(record),
                nciCode, efo
            )
        }
        private fun resolveSiteType(record:CSVRecord): CosmicType =
            CosmicType(
                "Site", record.get("SITE_PRIMARY"),
                record.get("SITE_SUBTYPE1"),
                record.get("SITE_SUBTYPE2"),
                record.get("SITE_SUBTYPE3")
            )

        private fun resolveHistologyType(record:CSVRecord): CosmicType =
            CosmicType(
                "Histology", record.get("HISTOLOGY"),
                record.get("HIST_SUBTYPE1"),
                record.get("HIST_SUBTYPE2"),
                record.get("HIST_SUBTYPE3")
            )

        private fun resolveCosmicSiteType(record:CSVRecord): CosmicType =
            CosmicType(
                "CosmicSite", record.get("SITE_PRIMARY_COSMIC"),
                record.get("SITE_SUBTYPE1_COSMIC"),
                record.get("SITE_SUBTYPE2_COSMIC"),
                record.get("SITE_SUBTYPE3_COSMIC")
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel =
            Companion::parseCsvRecord

    }
}

