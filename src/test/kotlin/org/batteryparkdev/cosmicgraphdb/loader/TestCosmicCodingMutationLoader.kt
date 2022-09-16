package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/cancer_gene_census.csv"
    TestCoreModelLoader(
        CosmicGeneCensus, filename, listOf("CosmicGene")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Gene Census data row count = ${it.getNodeCount()}")
    }
}
