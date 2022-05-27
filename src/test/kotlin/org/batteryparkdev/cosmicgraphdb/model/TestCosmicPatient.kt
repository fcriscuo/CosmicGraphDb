package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicPatient {

    fun loadCosmicPatientFile(filename:String):Int {
        val LIMIT = Long.MAX_VALUE
        deleteCosmicPatientNodes()
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map {CosmicPatient.parseValueMap(it)}
            .forEach { patient ->
                Neo4jConnectionService.executeCypherCommand(generatePatientTestCypher(patient))
                println("Loaded Cosmic patient: ${patient.patientId}")
            }
        return Neo4jConnectionService.executeCypherCommand("MATCH (cp:CosmicPatient) RETURN COUNT(cp)").toInt()
    }

    private fun generatePatientTestCypher(patient:CosmicPatient):String =
       "CALL apoc.merge.node([\"CosmicTumor\"], {tumor_id: ${patient.tumorId}}, " +
                "{},{} ) YIELD node as ${CosmicTumor.nodename} \n"
                    .plus(patient.generateCosmicPatientCypher())
                    .plus(" RETURN ${CosmicPatient.nodename}\n")


    private fun deleteCosmicPatientNodes() {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicPatient")
    }
}
fun main() {
    val filename  = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicSample.tsv")
    val recordCount =
        TestCosmicPatient().loadCosmicPatientFile(filename)
    println("Patient record count = $recordCount")
}