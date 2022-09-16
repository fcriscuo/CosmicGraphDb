package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf
import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/cancer_gene_census.csv"
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