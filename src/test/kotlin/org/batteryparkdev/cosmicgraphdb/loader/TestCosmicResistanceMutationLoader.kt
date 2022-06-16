package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicResistanceMutations.tsv"
    println("Loading Cosmic Resistance Mutation data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicResistanceMutation")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Resistance Mutation data row count = ${it.getNodeCount()}")
    }
}
