package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicStructExport.tsv"
    println("Loading Cosmic Struct data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicStruct")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Struct data row count = ${it.getNodeCount()}")
    }
}