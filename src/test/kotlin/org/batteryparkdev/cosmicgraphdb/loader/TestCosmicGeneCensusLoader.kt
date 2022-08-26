package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService

fun main () {
    val filename = CosmicFilenameService.resolveCosmicSampleFileLocation("cancer_gene_census.csv")
    println("Loading Cosmic Gene Census data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CCosmicGeneCensus","GenePublicationCollection",
        "GeneMutationCollection","CosmicAnnotation")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Gene Census data row count = ${it.getNodeCount()}")
    }
}