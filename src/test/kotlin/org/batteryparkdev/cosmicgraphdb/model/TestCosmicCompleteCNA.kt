package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/CosmicCompleteCNA.tsv"
    TestCoreModel(CosmicCompleteCNA.Companion).loadModels(filename)
}
