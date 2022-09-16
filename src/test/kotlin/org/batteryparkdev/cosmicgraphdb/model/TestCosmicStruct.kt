package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/CosmicStructExport.tsv"
    TestCoreModel(CosmicStruct.Companion).loadModels(filename)
}
