package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

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
        fun parseValueMap(value: Value): CosmicClassification {
            val nciCode = value["NCI_CODE"].asString() ?: "NS"
            val efo = value["EFO"].asString() ?: "NS"
            val phenoId = value["COSMIC_PHENOTYPE_ID"].asString() ?: "NS"

            return CosmicClassification(
                phenoId,
                resolveSiteType(value),
                resolveHistologyType(value),
                resolveCosmicSiteType(value),
                nciCode, efo
            )
        }

        private fun resolveSiteType(value: Value): CosmicType =
            CosmicType(
                "Site", value["SITE_PRIMARY"].asString(),
                value["SITE_SUBTYPE1"].asString(),
                value["SITE_SUBTYPE2"].asString(),
                value["SITE_SUBTYPE3"].asString()
            )

        private fun resolveHistologyType(value: Value): CosmicType =
            CosmicType(
                "Histology", value["HISTOLOGY"].asString(),
                value["HIST_SUBTYPE1"].asString(),
                value["HIST_SUBTYPE2"].asString(),
                value["HIST_SUBTYPE3"].asString()
            )

        private fun resolveCosmicSiteType(value: Value): CosmicType =
            CosmicType(
                "CosmicSite", value["SITE_PRIMARY_COSMIC"].asString(),
                value["SITE_SUBTYPE1_COSMIC"].asString(),
                value["SITE_SUBTYPE2_COSMIC"].asString(),
                value["SITE_SUBTYPE3_COSMIC"].asString()
            )

        fun generateChildRelationshipCypher(phenotypeId: String, parentNodeName: String): String {
            val relationship = "HAS_COSMIC_CLASSIFICATION"
            val relName = "rel_class"
            return "CALL apoc.merge.node(['CosmicClassification'], {phenotype_id: " +
                    " ${Neo4jUtils.formatPropertyValue(phenotypeId)}}, {},{}) " +
                    " YIELD node AS $nodename\n " +
                    " CALL apoc.merge.relationship( $parentNodeName, '$relationship', {}, " +
                    " {created: datetime()}, $nodename, {} ) " +
                    " YIELD rel AS $relName \n"
        }
    }
}

