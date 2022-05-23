package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value
/*
Represents the tumor data in the CosmicMutantExport or CosmicMutantExportCensus files
Key: tumorId
Relationships:
  Patient -[HAS_TUMOR] -> Tumor
  Tumor - [HAS_SAMPLE] -> Sample
 */
data class CosmicTumor(
    val tumorId: Int,
    val sampleId: Int,
    val tumorOrigin: String,
    val tumorSource: String,
    val tumorRemark: String,
    val patient: CosmicPatient
): CosmicModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicTumor", "tumor_id", tumorId.toString())

    fun generateCosmicTumorCypher():String {
        var cypher = ""
        if (Neo4jUtils.nodeExistsPredicate(getNodeIdentifier()).not()) {
            cypher = cypher.plus( generateTumorMergeCypher())
                .plus(patient.generateCosmicPatientCypher())
        }else {
            cypher = cypher.plus(generateTumorMatchCypher())
        }
            cypher = cypher.plus(generateTumorSampleRelationshipCypher())
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
                " ${Neo4jUtils.formatPropertyValue(tumorOrigin)} ," +
                " tumor_source: ${Neo4jUtils.formatPropertyValue(tumorSource)} , " +
                " tumor_remark: ${Neo4jUtils.formatPropertyValue(tumorRemark)} , " +
                "  created: datetime()},{}) YIELD node as $nodename \n"


   private  fun generateTumorMatchCypher(): String =
       "CALL apoc.merge.node ([\"CosmicTumor\"],{tumor_id: $tumorId},{} ) " +
               " YIELD node AS $nodename\n"

    private fun generateTumorSampleRelationshipCypher(): String {
        val relationship = "HAS_SAMPLE"
        val relname = "rel_mut_sample"
        return "CALL apoc.merge.relationship($nodename, '$relationship', " +
                    " {}, {created: datetime()}, ${CosmicSample.nodename},{} )" +
                    " YIELD rel AS $relname \n"
    }

    companion object : AbstractModel {
        const val nodename = "tumor"
        fun parseValueMap(value: Value): CosmicTumor =
            CosmicTumor(
                value["id_tumour"].asString().toInt(),
                value["sample_id"].asString().toInt(),
                value["Tumour origin"].asString(),
                value["tumour_source"].asString(),
                removeInternalQuotes(value["tumour_remark"].asString()),
                CosmicPatient.parseValueMap(value)
            )

        fun generatePlaceholderCypher(tumorId: Int)  = " CALL apoc.merge.node([\"CosmicTumor\"], " +
                " {tumor_id: $tumorId}, {created: datetime()},{modified: datetime()}) " +
                " YIELD node as $nodename  \n"

        fun generateChildRelationshipCypher (tumorId: Int, childLabel: String ) :String{
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_tumor"
            return  generatePlaceholderCypher(tumorId).plus(
            " CALL apoc.merge.relationship ($nodename, '$relationship', " +
                    " {}, {created: datetime()}, " +
                    " $childLabel, {} ) YIELD rel AS $relname \n")
        }

    }


}


