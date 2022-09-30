package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv"
    TestCoreModel(CosmicHallmark.Companion).loadModels(filename)
}
