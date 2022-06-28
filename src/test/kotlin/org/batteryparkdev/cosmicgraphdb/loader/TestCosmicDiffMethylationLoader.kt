package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicCompleteDifferentialMethylation.tsv"
    println("Loading Cosmic Complete Differential Methylation data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicDiffMethylation")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Complete Diff Methylation data row count = ${it.getNodeCount()}")
    }
}

