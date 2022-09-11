package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    // the tumor data are in the sample file
    val filename = if (args.isNotEmpty()) args[0] else
        "/Volumes/SSD870/COSMIC_rel96/sample/CosmicSample.tsv"
    TestCoreModel(CosmicTumor.Companion).loadModels(filename)
}