package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicFusionExport.tsv"
    println("Loading Cosmic Fusion data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicFusion")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Fusion data row count = ${it.getNodeCount()}")
    }
}

