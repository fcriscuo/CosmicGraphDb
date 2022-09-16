package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    // the tumor data are in the sample file
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/CosmicSample.tsv"
    TestCoreModel(CosmicTumor.Companion).loadModels(filename)
}