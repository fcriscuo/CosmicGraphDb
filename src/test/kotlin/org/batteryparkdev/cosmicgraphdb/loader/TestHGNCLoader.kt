package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestHGNCLoader {
}
fun main () {
  
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicHGNC.tsv")
    println("Loading Cosmic HGNC data from: $filename")
    CosmicHGNCLoader.loadCosmicHGNCData(filename)
}