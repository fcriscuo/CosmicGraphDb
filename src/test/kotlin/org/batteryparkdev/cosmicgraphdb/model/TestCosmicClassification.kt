package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/classification.csv"
    TestCoreModel(CosmicClassification.Companion).loadModels(filename)
}


