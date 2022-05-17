package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value
import java.util.*

data class CosmicHallmark(
    val hallmarkId: Int,   // needed to establish unique database identifier
    val geneSymbol: String, val cellType: String, val pubmedId: Int,
    val hallmark: String, val impact: String, val description: String
): CosmicModel
{
    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicHallmark", "hallmark_id",
            hallmarkId.toString())

    fun generateCosmicHallmarkCypher(): String =
        generateMergeCypher()
            .plus(generateMergeHallmarkCollectionCypher())
            .plus(generateHasHallmarkRelationshipCypher())
            .plus(" RETURN ${CosmicHallmark.nodename}")

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node( [\"CosmicHallmark\"]," +
                " {hallmark_id:  $hallmarkId} , " +
                "  {gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}, " +
                "  cell_type: ${Neo4jUtils.formatPropertyValue(cellType)}, " +
                "  pubmed_id: $pubmedId, " +
                "  hallmark: ${Neo4jUtils.formatPropertyValue(hallmark)}, " +
                "  impact: ${Neo4jUtils.formatPropertyValue(impact)}, " +
                "  description: ${Neo4jUtils.formatPropertyValue(description)}, " +
                "  created: datetime()}) YIELD node as ${CosmicHallmark.nodename} \n"


    private fun generateMergeHallmarkCollectionCypher(): String =
        " CALL apoc.merge.node( [\"CosmicHallmarkCollection\"], " +
                "{ gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}," +
                "  {created: datetime()}, {lastSeen: datetime()} ) YIELD node as $collectionname \n " +
                " CALL apoc.merge.node( [\"CosmicGene\"]," +
                "{  gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}}," +
                "  {created: datetime()},{} ) YIELD node as hallmark_gene \n" +
                " CALL apoc.merge.relationship (  hallmark_gene, 'HAS_HALLMARK_COLLECTION', " +
                " {}, {created: datetime()}, " +
                " $collectionname, {}) YIELD rel AS rel_coll \n"

    private fun generateHasHallmarkRelationshipCypher(): String {
        val relationship = " HAS_HALLMARK"
        val relName = "rel_hallmark"
        return "CALL apoc.merge.relationship (  $collectionname, '$relationship'," +
                " {}, {created: datetime()}, " +
                " ${CosmicHallmark.nodename}, {} )YIELD rel AS $relName \n"
    }

    companion object : AbstractModel {
        const val nodename = "hallmark"
        const val collectionname = "hallmark_collect"
        fun parseValueMap(value: Value): CosmicHallmark =
            CosmicHallmark(
                UUID.randomUUID().hashCode(),  // unique identifier for key
                value["GENE_NAME"].asString(),
                value["CELL_TYPE"].asString(),
                parseValidIntegerFromString(value["PUBMED_PMID"].asString()),
                removeInternalQuotes(value["HALLMARK"].asString()),
                value["IMPACT"].asString(),
                removeInternalQuotes(value["DESCRIPTION"].asString())
            )
    }
}
