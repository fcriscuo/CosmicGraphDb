package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "/Volumes/SSD870/COSMIC_rel96/sample/cancer_gene_census.csv"
    TestCoreModel(CosmicGeneCensus.Companion).loadModels(filename)
}
