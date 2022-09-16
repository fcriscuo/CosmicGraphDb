package org.batteryparkdev.cosmicgraphdb.loader

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv"
    println("Loading Cosmic Gene Census Hallmark data from: $filename")
    CosmicHallmarkLoader.processCosmicHallmarkFile(filename)
}

