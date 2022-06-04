package org.batteryparkdev.cosmicgraphdb.model

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

    fun generateCosmicPatientCypher() =
        generateMergeCypher()
            .plus(generateTumorRelationshipCypher())
            //.plus(" RETURN $nodename\n")

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
        fun parseValueMap(value: Value): CosmicPatient =
            CosmicPatient(
                value["sample_id"].asString().toInt(),
                value["id_individual"].asString().toInt(),
                value["id_tumour"].asString().toInt(),
                parseValidIntegerFromString(value["age"].asString()),
                value["ethnicity"].asString(),
                value["environmental_variables"].asString(),
                value["therapy"].asString(), value["family"].asString(),
                value["gender"].asString(),
                value["individual_remark"].asString(),
                convertYNtoBoolean(value["normal_tissue_tested"].asString())
            )

    }

}
