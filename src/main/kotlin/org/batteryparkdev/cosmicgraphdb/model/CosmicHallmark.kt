package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value
import java.util.*

data class CosmicHallmark(
    val hallmarkId: Int,   // needed to establish unique database identifier
    val geneSymbol: String, val cellType: String, val pubmedId: Int,
    val hallmark: String, val impact: String, val description: String
) : CosmicModel {
    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicHallmark", "hallmark_id",
            hallmarkId.toString()
        )

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
            .plus(generateMergeHallmarkCollectionCypher())
            .plus(generateHasHallmarkRelationshipCypher())
            .plus(" RETURN $nodename")

    override fun isValid(): Boolean = geneSymbol.isNotEmpty().and(hallmark.isNotEmpty())
    override fun getPubMedId(): Int = pubmedId

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node( [\"CosmicHallmark\"]," +
                " {hallmark_id:  $hallmarkId} , " +
                "  {gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}, " +
                "  cell_type: ${Neo4jUtils.formatPropertyValue(cellType)}, " +
                "  pubmed_id: $pubmedId, " +
                "  hallmark: ${Neo4jUtils.formatPropertyValue(hallmark)}, " +
                "  impact: ${Neo4jUtils.formatPropertyValue(impact)}, " +
                "  description: ${Neo4jUtils.formatPropertyValue(description)}, " +
                "  created: datetime()}) YIELD node as $nodename \n"


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
                " $nodename, {} )YIELD rel AS $relName \n"
    }

    companion object : AbstractModel {
        const val nodename = "hallmark"
        const val collectionname = "hallmark_collect"


        fun parseCSVRecord(record: CSVRecord): CosmicHallmark =
            CosmicHallmark(
                UUID.randomUUID().hashCode(),  // unique identifier for key
                record.get("GENE_NAME"),
                record.get("CELL_TYPE"),
                parseValidIntegerFromString(record.get("PUBMED_PMID")),
                removeInternalQuotes(record.get("HALLMARK")),
                resolveImpactProperty(record),
                removeInternalQuotes(record.get("DESCRIPTION"))
            )
         private fun resolveImpactProperty(record: CSVRecord):String =
            when (record.isMapped("IMPACT")) {
                true -> record.get("IMPACT")
                false -> "NS"
            }
    }
}
