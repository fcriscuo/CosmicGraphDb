package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicHGNC(
    val cosmicId: Int,
    val hgncGeneSymbol: String,
    val entrezId: Int,
    val hgncId: Int,
    val isMutated: Boolean,
    val isCancerCensus: Boolean,
    val isExpertCurrated: Boolean
): CosmicModel {
    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicHGNC", "cosmicId", cosmicId.toString())

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(generateGeneRelationshipCypher())
            .plus(generateEntrezRelationshipCypher())
            .plus(" RETURN ${CosmicHGNC.nodename}")

    override fun isValid(): Boolean = hgncGeneSymbol.isNotEmpty().and(cosmicId > 0)
    override fun getPubMedId(): Int = 0

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node( [\"CosmicHGNC\"], " +
                " {hgnc_id: $hgncId}," +
                " {cosmic_id: $cosmicId, gene_symbol: ${Neo4jUtils.formatPropertyValue(hgncGeneSymbol)}, " +
                " entrez_id: $entrezId, is_mutaated: $isMutated, is_cancer_census: $isCancerCensus, " +
                " is_expert_currated: $isExpertCurrated, " +
                "  created: datetime()}) YIELD node as ${CosmicHGNC.nodename} \n"

    /*
   Function to generate Cypher commands to create a
   CosmicGene - [HAS_HGNC] -> CosmicHGNC relationship
    */
    private fun generateGeneRelationshipCypher(): String =
        when (isCancerCensus) {
            true -> CosmicGeneCensus.generateGeneParentRelationshipCypher(hgncGeneSymbol, CosmicHGNC.nodename)
            false -> " "
        }

    //HGNC - [HAS_ENTREZ]  -> Entrez  relationship
    private fun generateEntrezRelationshipCypher(): String =
        when (entrezId > 0) {
            true -> Entrez.generateHasEntrezRelationship(entrezId, CosmicHGNC.nodename)
            false -> " "
        }

    companion object : AbstractModel {
        const val nodename = "hgnc"

        fun parseValueMap(value: Value): CosmicHGNC =
            CosmicHGNC(
                parseValidIntegerFromString(value["COSMIC_ID"].asString()),
                value["COSMIC_GENE_NAME"].asString(),
                parseValidIntegerFromString(value["Entrez_id"].asString()),
                parseValidIntegerFromString(value["HGNC_ID"].asString()),
                convertYNtoBoolean(value["Mutated?"].asString()),
                convertYNtoBoolean(value["Cancer_census?"].asString()),
                convertYNtoBoolean(value["Expert Curated?"].asString())
            )

        private fun generateMatchHGNCNodeCypher(hgncId: Int): String = " CALL apoc.merge.node([\"CosmicHGNC\"]," +
                " { hgnc_id: $hgncId}, {created: datetime()},{} ) " +
                " YIELD node as ${CosmicHGNC.nodename} \n"

        fun generateHasHGNCRelationshipCypher(hgncId: Int, parentNodeName: String): String {
            val relationship = "HAS_HGNC"
            val relName = "rel_hgnc"
            return generateMatchHGNCNodeCypher(hgncId).plus(
                " CALL apoc.merge.relationship ($parentNodeName, '$relationship' ," +
                        " {}, {created: datetime()}," +
                        " ${CosmicHGNC.nodename}, {}) YIELD rel AS $relName \n"
            )
        }
    }


}
