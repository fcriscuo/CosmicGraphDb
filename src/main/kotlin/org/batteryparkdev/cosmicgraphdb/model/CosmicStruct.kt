package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

/*
Represents the data in the CosmicStructExport file
Key: mutationId
Node Relationships: Mutation -[HAS_STRUCT] -> Struct
                    Struct - [HAS_PUBLICATION] -> Publication
 */

data class CosmicStruct(
    val mutationId: Int,
    val sampleId: Int,
    val tumorId: Int,
    val mutationType: String,
    val description: String,
    val pubmedId: Int
) : CosmicModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicStruct", "mutation_id", mutationId.toString(),
            resolveStructType()
        )

    fun generateStructCypher(): String = generateMergeCypher()
        .plus(generateMutationRelationshipCypher())
        .plus(" RETURN $nodename\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicStruct\"," +
            "${Neo4jUtils.formatPropertyValue(resolveStructType())} ], " +
            " {mutation_id: $mutationId}, {sample_id: $sampleId, " +
            " tumor_id: $tumorId, mutation_type: ${Neo4jUtils.formatPropertyValue(mutationType)}, " +
            " description: ${Neo4jUtils.formatPropertyValue(description)}, " +
            "  pubmed_id: $pubmedId, created: datetime() }, " +
            " { last_mod: datetime()}) YIELD node AS $nodename \n"



    private fun generateMutationRelationshipCypher(): String =
        CosmicMutation.generateChildRelationshipCypher(mutationId, nodename)

    private fun resolveStructType(): String =
        with(description) {
            when {
                endsWith("bkpt") -> "Breakpoint"
                endsWith("del") -> "Deletion"
                endsWith("ins") -> "Insertion"
                else -> "Unspecified"
            }
        }

    companion object : AbstractModel {
        const val nodename = "struct"
        fun parseValueMap(value: Value): CosmicStruct =
            CosmicStruct(
                value["MUTATION_ID"].asString().toInt(),
                value["ID_SAMPLE"].asString().toInt(),
                value["ID_TUMOUR"].asString().toInt(),
                value["Mutation Type"].asString(),
                value["description"].asString(),
                parseValidIntegerFromString(value["PUBMED_PMID"].asString())
            )

        private fun generateMatchCosmicStructCypher(mutationId: Int): String  =
            "CALL apoc.merge.node( [\"CosmicStruct\"], " +
                    " {mutation_id: $mutationId},  {created: datetime()},{}) " +
                    " YIELD node AS $nodename\n "

        fun generateChildRelationshipCypher(mutationId: Int, childLabel: String) : String {
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_struct"
            return generateMatchCosmicStructCypher(mutationId).plus(
                "CALL apoc.merge.relationship($nodename, '$relationship', " +
                        " {}, {created: datetime()}, ${childLabel.lowercase()},{} )" +
                        " YIELD rel as $relname \n")
        }
    }
}