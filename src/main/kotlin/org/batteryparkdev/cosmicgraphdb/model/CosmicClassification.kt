package org.batteryparkdev.cosmicgraphdb.model

import org.neo4j.driver.Value

/*
Data class responsible for modelling an entry in the CosmicClassification file
Generates APOC-based cypher statement needed to merge these data into the Neo4j
database
 */
data class CosmicClassification(
    val cosmicPhenotypeId: String,
    val siteType: CosmicType,
    val histologyType: CosmicType,
    val cosmicSiteType: CosmicType,
    val cosmicHistologyType: CosmicType,
    val nciCode: String,
    val efoUrl: String
) {
     val nodeName = "class_node"

    fun resolveClassificationId(): Int =
         cosmicPhenotypeId.plus(siteType.primary)
             .plus(histologyType.primary).hashCode()

    fun generateClassificationCypher():String =
        generateMergeCypher()
            .plus(siteType.generateParentRelationshipCypher(nodeName))
            .plus(histologyType.generateParentRelationshipCypher(nodeName))
            .plus(" RETURN $nodeName")


    private fun generateMergeCypher():String = "CALL apoc.merge.node([\"CosmicClassification\"]," +
            "{ classification_id: ${resolveClassificationId()}}," +
            " { phenotype_id: \"${cosmicPhenotypeId}\", " +
            " nci_code: \"${nciCode}\"," +
            " efo_url: \"${efoUrl}\"," +
            " created: datetime() }," +
            "{ last_mod: datetime()}) YIELD node AS $nodeName \n "

    companion object : AbstractModel {

        fun parseValueMap(value: Value): CosmicClassification {
            val nciCode =value["NCI_CODE"].asString() ?: "NS"
            val efo = value["EFO"].asString() ?: "NS"
            val phenoId = value["COSMIC_PHENOTYPE_ID"].asString() ?: "NS"

            return CosmicClassification(phenoId,
                resolveSiteType(value),
                resolveHistologyType(value),
                resolveCosmicSiteType(value),
                resolveCosmicHistologyType(value),
                nciCode, efo
            )
        }

        private fun resolveSiteType(value:Value): CosmicType =
            CosmicType("Site", value["SITE_PRIMARY"].asString(),
                value["SITE_SUBTYPE1"].asString(),
                value["SITE_SUBTYPE2"].asString(),
                value["SITE_SUBTYPE3"].asString()
            )

        private fun resolveHistologyType(value:Value): CosmicType =
            CosmicType("Histology", value["HISTOLOGY"].asString(),
                value["HIST_SUBTYPE1"].asString(),
                value["HIST_SUBTYPE2"].asString(),
                value["HIST_SUBTYPE3"].asString()
            )

        private fun resolveCosmicSiteType(value:Value): CosmicType =
            CosmicType(
                "CosmicSite", value["SITE_PRIMARY_COSMIC"].asString(),
                value["SITE_SUBTYPE1_COSMIC"].asString(),
                value["SITE_SUBTYPE2_COSMIC"].asString(),
                value["SITE_SUBTYPE3_COSMIC"].asString()
            )
        private fun resolveCosmicHistologyType(value:Value): CosmicType =
            CosmicType("CosmicHistology", value["HISTOLOGY_COSMIC"].asString(),
                value["HIST_SUBTYPE1_COSMIC"].asString(),
                value["HIST_SUBTYPE2_COSMIC"].asString(),
                value["HIST_SUBTYPE3_COSMIC"].asString()
            )
    }
}

