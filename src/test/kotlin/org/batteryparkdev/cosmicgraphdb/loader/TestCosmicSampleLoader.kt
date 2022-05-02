package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicSampleLoader {
}
fun main() {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("file.cosmic.sample")
    println("Loading Cosmic Sample data from: $filename")
    CosmicSampleLoader.processCosmicSampleData(filename)
}