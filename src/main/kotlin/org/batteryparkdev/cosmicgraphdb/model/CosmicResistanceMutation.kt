package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.service.generateNeo4jNodeKey
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicResistanceMutation(
    val resistanceId: String,
    val mutationId: Int,
    val sampleId: Int,
    val geneSymbol: String,
    val transcript: String,
    val drugName: String,
    val pubmedId: Int
): CosmicModel
{
    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("DrugResistance", "resistance_id",
            resistanceId.toString())

    fun generateCosmicResistanceCypher(): String =generateMergeCypher()
        .plus(CosmicMutation.generateChildRelationshipCypher(mutationId, nodename))
        .plus(CosmicSample.generateChildRelationshipCypher(sampleId, nodename))
        .plus(generateDrugRelationshipCypher())
        .plus(" RETURN node AS $nodename\n")

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"DrugResistance\"], " +
                " {resistance_id: ${Neo4jUtils.formatPropertyValue(resistanceId)}}," +
                "  { mutation_id: $mutationId, " +
                " gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}," +
                " transcript: ${Neo4jUtils.formatPropertyValue(transcript)}, " +
                " pubmed_id: $pubmedId, " +
                "  created: datetime()}) YIELD node as $nodename \n"

    private fun generateMatchDrugCypher(): String  =
        "CALL apoc.merge.node( [\"CosmicDrug\"], " +
                " {drug_name: ${Neo4jUtils.formatPropertyValue(drugName.lowercase())}},  {created: datetime()},{} )" +
                " YIELD node AS $nodename \n"

    private fun generateDrugRelationshipCypher(): String {
        val relationship = "RESISTANT_TO"
        val relname = "rel_drug"
        return generateMatchDrugCypher().plus(
            "CALL apoc.merge.relationship($nodename, '$relationship', " +
                    " {}, {created: datetime()}, drug,{} )" +
                    " YIELD rel as $relname \n"
        )
    }

    companion object : AbstractModel {
        const val nodename = "resistance"
        fun parseValueMap(value: Value): CosmicResistanceMutation =
            CosmicResistanceMutation(
                generateNeo4jNodeKey(),
                value["MUTATION_ID"].asString().toInt(),
                value["Sample ID"].asString().toInt(),
                value["Gene Name"].asString(),
                value["Transcript"].asString(),
                value["Drug Name"].asString(),
                value["Pubmed Id"].asString().toInt()
            )
    }
}

