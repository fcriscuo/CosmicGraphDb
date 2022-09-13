package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf
import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService

fun main() {
    val filename = "./data/cancer_gene_census.csv"
    //println("Loading Cosmic Gene Census data from: $filename")
    TestCoreModelLoader(
        CosmicGeneCensus, filename,
        listOf(
            "CosmicGeneCensus", "GenePublicationCollection",
            "GeneMutationCollection", "CosmicAnnotation"
        )
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Gene Census data row count = ${it.getNodeCount()}")
    }
}