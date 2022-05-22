package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value
/*
Represents the tumor data in the CosmicMutantExport or CosmicMutantExportCensus files
Key: tumorId
Relationships:  Tumor - [HAS_SAMPLE] -> Sample
 */
data class CosmicTumor(
    val tumorId: Int, val sampleId: Int,
    val tumorOrigin: String, val age: Int,
): CosmicModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicTumor", "tumor_id", tumorId.toString())

    fun generateCosmicTumorCypher():String {
        val cypher = when (Neo4jUtils.nodeExistsPredicate(getNodeIdentifier())) {
            true -> generateTumorMatchCypher().plus(generateTumorSampleRelationshipCypher())
            false -> generateTumorMergeCypher().plus(generateTumorSampleRelationshipCypher())
        }
        return cypher
    }

    /*
    Function to generate Cypher statements to create tumor
    nodes and relationships in the Neo4j database if the tumor id is novel
    n.b. the generated Cypher is intended to be used within a larger
    transaction and as a result it does not have a RETURN component
     */
   private fun generateTumorMergeCypher(): String =
        " CALL apoc.merge.node( [\"CosmicTumor\"], " +
                "{tumor_id: $tumorId} ," +
                " {tumor_origin: " +
                "${Neo4jUtils.formatPropertyValue(tumorOrigin)} ," +
                " age: $age , " +
                "  created: datetime()},{}) YIELD node as ${CosmicTumor.nodename} \n"


   private  fun generateTumorMatchCypher(): String =
       "CALL apoc.merge.node ([\"CosmicTumor\"],{tumor_id: $tumorId},{} ) " +
               " YIELD node AS ${CosmicTumor.nodename}\n"

    private fun generateTumorSampleRelationshipCypher(): String {
        val relationship = "HAS_SAMPLE"
        val relname = "rel_mut_sample"
        return CosmicSample.generateMatchCosmicSampleCypher(sampleId)
            .plus("CALL apoc.merge.relationship(${CosmicTumor.nodename}, '$relationship', " +
                    " {}, {created: datetime()}, ${CosmicSample.nodename},{} )" +
                    " YIELD rel AS $relname \n")
    }

    companion object : AbstractModel {
        const val nodename = "tumor"
        fun parseValueMap(value: Value): CosmicTumor =
            CosmicTumor(
                value["ID_tumour"].asString().toInt(),
                value["ID_sample"].asString().toInt(),
                value["Tumour origin"].asString(),
                parseValidIntegerFromString(value["Age"].asString())
            )

        fun generatePlaceholderCypher(tumorId: Int)  = " CALL apoc.merge.node([\"CosmicTumor\"], " +
                " {tumor_id: $tumorId}, {created: datetime()},{modified: datetime()}) " +
                " YIELD node as ${CosmicTumor.nodename}  \n"

        fun generateChildRelationshipCypher (tumorId: Int, childLabel: String ) :String{
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_tumor"
            return  generatePlaceholderCypher(tumorId).plus(
            " CALL apoc.merge.relationship (${CosmicTumor.nodename}, '$relationship', " +
                    " {}, {created: datetime()}, " +
                    " $childLabel, {} ) YIELD rel AS $relname \n")
        }

    }


}


