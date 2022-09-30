package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/cancer_gene_census.csv"
    TestCoreModel(CosmicGeneCensus.Companion).loadModels(filename)
}
