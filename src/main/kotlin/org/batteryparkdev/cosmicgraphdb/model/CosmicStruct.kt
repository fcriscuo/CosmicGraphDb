package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

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
        //.plus(generateTumorRelationshipCypher())
        .plus(" RETURN ${CosmicStruct.nodename}\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicStruct\"," +
            "${Neo4jUtils.formatPropertyValue(resolveStructType())} ], " +
            " {mutation_id: $mutationId}, {sample_id: $sampleId, " +
            " tumor_id: $tumorId, mutation_type: ${Neo4jUtils.formatPropertyValue(mutationType)}, " +
            " description: ${Neo4jUtils.formatPropertyValue(description)}, " +
            "  pubmed_id: $pubmedId, created: datetime() }, " +
            " { last_mod: datetime()}) YIELD node AS $nodename \n"

    private fun generateTumorRelationshipCypher(): String =
        CosmicTumor.generateChildRelationshipCypher(tumorId, nodename)

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

        private fun generateStructPlaceholderCypher(mutationId: Int): String {
           val structId = NodeIdentifier("CosmicStruct", "mutation_id", mutationId.toString())
            return when(Neo4jUtils.nodeExistsPredicate(structId)) {
                false -> "CALL apoc.merge.node( [\"CosmicStruct\",\"Breakpoint\"], " +
                        " {mutation_id: $mutationId},  {created: datetime()},{}) " +
                        " YIELD node AS ${CosmicStruct.nodename}\n "
                true -> "CALL apoc.merge.node( [\"CosmicStruct\"], " +
                        " {mutation_id: $mutationId},  {},{}) " +
                        " YIELD node AS ${CosmicStruct.nodename}\n "
            }
        }

        fun generateChildRelationshipCypher(mutationId: Int, childLabel: String) : String {
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_struct"
            return generateStructPlaceholderCypher(mutationId).plus(
                "CALL apoc.merge.relationship(${CosmicStruct.nodename}, '$relationship', " +
                        " {}, {created: datetime()}, ${childLabel.lowercase()},{} )" +
                        " YIELD rel as $relname \n")
        }
    }
}