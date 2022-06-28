package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicCompleteGeneExpression.tsv"
    println("Loading Cosmic Complete Gene Expression data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CompleteGeneExpression")).let {
        it.loadCosmicFile()
        println("Loaded Cosmic Complete Gene Expression data row count = ${it.getNodeCount()}")
    }
}
