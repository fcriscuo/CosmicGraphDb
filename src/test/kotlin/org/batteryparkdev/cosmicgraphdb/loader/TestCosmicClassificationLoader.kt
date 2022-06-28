package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "classification.csv"
    println("Loading Cosmic Classification data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmiClassification")).let {
        it.loadCosmicFile()
        println("Loaded CosmicClassification data row count = ${it.getNodeCount()}")
    }
}
