package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicNCV.tsv"
    println("Loading Cosmic NCV data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicNCV")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic NCV data row count = ${it.getNodeCount()}")
    }
}