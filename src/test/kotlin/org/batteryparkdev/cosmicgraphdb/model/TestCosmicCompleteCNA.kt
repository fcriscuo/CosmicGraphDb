package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "/Volumes/SSD870/COSMIC_rel96/sample/CosmicCompleteCNA.tsv"
    TestCoreModel(CosmicCompleteCNA.Companion).loadModels(filename)
}
