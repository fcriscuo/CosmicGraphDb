package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService

fun main() {
    val filenameRunmodePair = Pair("Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv", "complete")
    val filename = "/Volumes/SSD870/COSMIC_rel96/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv"
    println("Loading Cosmic Gene Census Hallmark data from: $filename")
    CosmicHallmarkLoader.processCosmicHallmarkFile(filename)
}

