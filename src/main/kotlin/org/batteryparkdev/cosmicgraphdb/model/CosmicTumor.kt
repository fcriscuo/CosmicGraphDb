package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
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
    val tumorSource: String,
    val tumorRemark: String,
    val patient: CosmicPatient
): CosmicModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicTumor", "tumor_id", tumorId.toString())

    override fun isValid(): Boolean = patient.isValid().and(tumorId > 0)
    override fun getPubMedId(): Int = 0

    override fun generateLoadCosmicModelCypher():String {
        var cypher = ""
        if (Neo4jUtils.nodeExistsPredicate(getNodeIdentifier()).not()) {
            cypher = cypher.plus( generateTumorMergeCypher())
                .plus(patient.generateLoadCosmicModelCypher())
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
                " { tumor_source: ${Neo4jUtils.formatPropertyValue(tumorSource)} , " +
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

        fun parseCSVRecord(record: CSVRecord): CosmicTumor =
            CosmicTumor(
                record.get("id_tumour").toInt(),
                record.get("sample_id").toInt(),
                record.get("tumour_source"),
                removeInternalQuotes(record.get("tumour_remark")),
                CosmicPatient.parseCSVRecord(record)
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


