package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicHGNC.tsv"
    println("Loading Cosmic HGNC data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicHGNC")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic HGNC data row count = ${it.getNodeCount()}")
    }
}