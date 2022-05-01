package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicGeneCensusLoader {
}

fun main () {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("file.cosmic.cancer.gene.census")
    println("Loading Cosmic Gene Census data from: $filename")
    CosmicGeneCensusLoader.loadCosmicGeneCensusData(filename)
}