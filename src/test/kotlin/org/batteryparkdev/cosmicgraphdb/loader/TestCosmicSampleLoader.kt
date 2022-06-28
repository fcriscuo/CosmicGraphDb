package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicSample.tsv"
    println("Loading Cosmic Sample data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicSample")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Sample data row count = ${it.getNodeCount()}")
    }
}