package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv"
    println("Loading Cosmic Gene Census Hallmark data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicHallmark")).let {
        it.loadCosmicFile()
        println("Loaded CosmicCGeneCensusHallmark data row count = ${it.getNodeCount()}")
    }
}
