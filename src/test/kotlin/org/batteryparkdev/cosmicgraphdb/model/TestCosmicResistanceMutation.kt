package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/CosmicResistanceMutations.tsv"
    TestCoreModel(CosmicResistanceMutation.Companion).loadModels(filename)
}
