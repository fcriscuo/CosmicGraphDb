package org.batteryparkdev.cosmicgraphdb.poc

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.model.*
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import org.neo4j.driver.Value

class TestCosmicModel (val filename: String) {

    fun processCosmicFile(){
        ApocFileReader.processDelimitedFile(filename)
            .map { record -> record.get("map") }
            .map { parseCosmicModel(it) }
            .forEach { model -> println(model) }
    }

    private fun parseCosmicModel(value: Value) : CosmicModel {
        return when (filename) {
           "CosmicFusion.tsv" -> CosmicFusion.parseValueMap(value)
            "classification.csv" -> CosmicClassification.parseValueMap(value)
            "cancer_gene_census.csv" -> CosmicGeneCensus.parseValueMap(value)
            else -> CosmicGeneCensus.parseValueMap(value)
        }
    }
}

fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("cancer_gene_census.csv")
    TestCosmicModel(filename).processCosmicFile()
}