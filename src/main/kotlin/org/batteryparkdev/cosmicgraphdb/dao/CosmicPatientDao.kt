package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicPatient
import org.batteryparkdev.cosmicgraphdb.model.CosmicTumor
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

class CosmicPatientDao (private val patient: CosmicPatient) {

    fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
      // CosmicPatient is loaded with CosmicTumor
    // A Neo4j RETURN clause is not needed here

    private fun generateMergeCypher(): String =
        "CALL apoc.merge.node([\"CosmicPatient\", \"Patient\"], " +
                " {patient_id: ${patient.patientId} }, " +
                "{ age: ${patient.age} , " +
                " tumor_id: ${patient.tumorId}, " +
                " ethnicity: ${patient.ethnicity.formatNeo4jPropertyValue()}, " +
                " environmental_variables: ${patient.environmental_variables.formatNeo4jPropertyValue()}, " +
                " family: ${patient.family.formatNeo4jPropertyValue()}, " +
                " gender: ${patient.gender.formatNeo4jPropertyValue()}, " +
                " therapy: ${patient.therapy.formatNeo4jPropertyValue()}, " +
                " individual_remark: ${patient.individual_remark.formatNeo4jPropertyValue()}, " +
                " normal_tissue_tested: ${patient.normal_tissue_tested} , " +
                " created: datetime() }," +
                "{ last_mod: datetime()}) YIELD node AS ${CosmicPatient.nodename} \n "

    companion object: CoreModelDao{

        private fun completePatientRelationships(model: CoreModel) {
            generateTumorRelationshipCypher(model)
        }
        // Patient - HAS_TUMOR -> Tumor
        private fun generateTumorRelationshipCypher(model: CoreModel) {
            if (model is CosmicPatient){
                val tumorNode = CosmicTumor.generateNodeIdentifierByValue(model.tumorId.toString())
                NodeIdentifierDao.defineRelationship(RelationshipDefinition(model.getNodeIdentifier(),
                 tumorNode, "HAS_TUMOR"))
            }
        }
        override val modelRelationshipFunctions: (CoreModel) -> Unit
            = ::completePatientRelationships

    }
}