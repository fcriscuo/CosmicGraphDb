package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicPatient(
    val sampleId: Int,
    val patientId: Int,
    val tumorId: Int,
    val age: Int,
    val ethnicity: String,
    val environmental_variables: String,
    val therapy: String,
    val family: String,
    val gender: String,
    val individual_remark: String,
    val normal_tissue_tested: Boolean
): CosmicModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicPatient", "patient_id", patientId.toString())

    override fun generateLoadCosmicModelCypher() =
        generateMergeCypher()
            .plus(generateTumorRelationshipCypher())
            //.plus(" RETURN $nodename\n")

    override fun isValid(): Boolean = patientId > 0 && tumorId > 0
    override fun getPubMedId(): Int  = 0

    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node([\"CosmicPatient\"], " +
                " {patient_id: $patientId}, " +
                "{ age: $age, ethnicity: ${Neo4jUtils.formatPropertyValue(ethnicity)}, " +
                " environmental_variables: ${Neo4jUtils.formatPropertyValue(environmental_variables)}, " +
                " family: ${Neo4jUtils.formatPropertyValue(family)}, " +
                " gender: ${Neo4jUtils.formatPropertyValue(gender)}, " +
                " therapy: ${Neo4jUtils.formatPropertyValue(therapy)}, " +
                " individual_remark: ${Neo4jUtils.formatPropertyValue(individual_remark)}, " +
                " normal_tissue_tested: $normal_tissue_tested, " +
                " created: datetime() }," +
                "{ last_mod: datetime()}) YIELD node AS $nodename \n "

    private fun generateTumorRelationshipCypher():String
       =  "CALL apoc.merge.relationship( $nodename, " +
                    " 'HAS_TUMOR', {}, {created: datetime()}," +
                    " ${CosmicTumor.nodename},{} ) " +
                    " YIELD rel as pat_rel \n"

    companion object : AbstractModel {
        val nodename = "patient"

        fun parseCSVRecord(record: CSVRecord): CosmicPatient =
            CosmicPatient(
                record.get("sample_id").toInt(),
                record.get("id_individual").toInt(),
                record.get("id_tumour").toInt(),
                parseValidIntegerFromString(record.get("age")),
                record.get("ethnicity"),
                record.get("environmental_variables"),
                record.get("therapy"), record.get("family"),
                record.get("gender"),
                record.get("individual_remark"),
                convertYNtoBoolean(record.get("normal_tissue_tested"))
            )

    }

}
