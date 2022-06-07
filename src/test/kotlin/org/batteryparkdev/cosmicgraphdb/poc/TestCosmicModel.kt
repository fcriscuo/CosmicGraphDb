package org.batteryparkdev.cosmicgraphdb.poc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.model.*
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import org.neo4j.driver.Value

class TestCosmicModel (val filename: String) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicFile() =
        produce<CosmicModel> {
            val path = ConfigurationPropertiesService.resolveCosmicSampleFileLocation(filename)
            ApocFileReader.processDelimitedFile(path)
                .map { record -> record.get("map") }
                .map { parseCosmicModel(it) }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }

    private fun processCosmicFile (){
        ApocFileReader.processDelimitedFile(filename)
            .map { record -> record.get("map") }
            .map { parseCosmicModel(it) }
            .forEach { model -> println(model) }
    }

    private fun parseCosmicModel(value: Value) : CosmicModel {
        return when (filename) {
           "CosmicFusionExport.tsv" -> CosmicFusion.parseValueMap(value)
            "classification.csv" -> CosmicClassification.parseValueMap(value)
            "cancer_gene_census.csv" -> CosmicGeneCensus.parseValueMap(value)
            else -> CosmicGeneCensus.parseValueMap(value)
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processModelObjects(models: ReceiveChannel<CosmicModel>) =
        produce<String> {
            for (model in models){
                send(model.generateLoadCosmicModelCypher())
                delay(20L)
            }
        }

    fun loadCosmicFile() = runBlocking {
        val commands = processModelObjects(parseCosmicFile())
        for (command in commands) {
            println("Cypher Command: $command")
        }
    }
}

fun main() {
    val filename = "cancer_gene_census.csv"
    TestCosmicModel(filename).loadCosmicFile()
}