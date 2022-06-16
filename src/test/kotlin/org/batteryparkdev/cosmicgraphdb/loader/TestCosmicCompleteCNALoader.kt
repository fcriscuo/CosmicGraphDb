package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicCompleteCNA.tsv"
    println("Loading Cosmic Complete CNA data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicCompleteCNA")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Complete CNA data row count = ${it.getNodeCount()}")
    }
}


