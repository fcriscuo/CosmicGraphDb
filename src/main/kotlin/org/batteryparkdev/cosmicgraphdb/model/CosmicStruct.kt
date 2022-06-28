package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
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

    override fun isValid(): Boolean = sampleId > 0
    override fun getPubMedId(): Int = pubmedId

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
        .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
        .plus(" RETURN $nodename\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicStruct\"," +
            "${Neo4jUtils.formatPropertyValue(resolveStructType())} ], " +
            " {mutation_id: $mutationId}, {sample_id: $sampleId, " +
            " tumor_id: $tumorId, mutation_type: ${Neo4jUtils.formatPropertyValue(mutationType)}, " +
            " description: ${Neo4jUtils.formatPropertyValue(description)}, " +
            "  pubmed_id: $pubmedId, created: datetime() }, " +
            " { last_mod: datetime()}) YIELD node AS $nodename \n"

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

        fun parseCSVRecord(record: CSVRecord): CosmicStruct =
            CosmicStruct(
                record.get("MUTATION_ID").toInt(),
                record.get("ID_SAMPLE").toInt(),
                record.get("ID_TUMOUR").toInt(),
                record.get("Mutation Type"),
                record.get("description"),
                parseValidIntegerFromString(record.get("PUBMED_PMID"))
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