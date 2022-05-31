package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.util.concurrent.TimeUnit

class TestCosmicResistanceMutationLoader {

    fun loadCosmicDrugResistanceMutationFile(filename: String): Int {
        deleteCosmicResistanceMutationNodes()
        CosmicResistanceMutationLoader.loadCosmicResistanceMutationFile(filename)
        return Neo4jConnectionService.executeCypherCommand("MATCH (crm:CosmicResistanceMutation) RETURN COUNT(crm)").toInt()
    }

    private fun deleteCosmicResistanceMutationNodes () {
        Neo4jUtils.detachAndDeleteNodesByName("CosmicResistanceMutation")
    }
}
fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicResistanceMutations.tsv")
    println("Loading Cosmic Resistance Mutations data from: $filename")
    val stopwatch = Stopwatch.createStarted()
    val rowCount = TestCosmicResistanceMutationLoader().loadCosmicDrugResistanceMutationFile(filename)
    println("Loaded $rowCount CosmicResistanceMutation nodes in ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")

}