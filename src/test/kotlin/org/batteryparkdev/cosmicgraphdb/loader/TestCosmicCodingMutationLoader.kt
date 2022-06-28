package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicMutantExportCensus.tsv"
    println("Loading Cosmic Coding Mutation data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmiCodingMutation")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Coding Mutation data row count = ${it.getNodeCount()}")
    }
}
