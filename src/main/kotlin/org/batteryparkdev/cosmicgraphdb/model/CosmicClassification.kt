package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

data class CosmicClassification(
    val cosmicPhenotypeId: String,
    val siteType: CosmicType,
    val histologyType: CosmicType,
    val cosmicSiteType: CosmicType,
    val nciCode: String,
    val efoUrl: String
) : CosmicModel {
    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicClassification", "phenotype_id",
            cosmicPhenotypeId
        )
    override fun isValid(): Boolean = cosmicPhenotypeId.isNotEmpty()
    override fun getPubMedId(): Int = 0

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(siteType.generateCosmicTypeCypher(CosmicClassification.nodename))
            .plus(histologyType.generateCosmicTypeCypher(CosmicClassification.nodename))
            .plus(" RETURN ${CosmicClassification.nodename}\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicClassification\"]," +
            "{ phenotype_id: \"${cosmicPhenotypeId}\"}," +
            " { nci_code: \"${nciCode}\"," +
            " efo_url: \"${efoUrl}\"," +
            " created: datetime() }," +
            "{ last_mod: datetime()}) YIELD node AS $nodename \n "

    companion object : AbstractModel {
        val nodename = "classification"

        fun parseCSVRecord(record:CSVRecord): CosmicClassification {
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

        fun generateChildRelationshipCypher(phenotypeId: String, parentNodeName: String): String {
            val relationship = "HAS_COSMIC_CLASSIFICATION"
            val relName = "rel_class"
            return "CALL apoc.merge.node(['CosmicClassification'], {phenotype_id: " +
                    " ${phenotypeId.formatNeo4jPropertyValue()}}, {},{}) " +
                    " YIELD node AS $nodename\n " +
                    " CALL apoc.merge.relationship( $parentNodeName, '$relationship', {}, " +
                    " {created: datetime()}, $nodename, {} ) " +
                    " YIELD rel AS $relName \n"
        }
    }
}

