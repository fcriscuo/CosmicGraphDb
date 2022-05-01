package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.property.service.ConfigurationPropertiesService

class TestCosmicCompleteGeneExpressionLoader {
}
fun main(args: Array<String>) {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("file.cosmic.complete.gene.expression")
    println("Loading COSMIC complete gene expression data from: $filename")
    CosmicCompleteGeneExpressionLoader.loadCosmicCompleteGeneExpressionData(filename)
}