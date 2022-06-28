package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.nonEmptyListOf

fun main () {
    val filename = "CosmicBreakpointsExport.tsv"
    println("Loading Cosmic Breakpoint data from: $filename")
    TestCosmicLoader(filename, nonEmptyListOf("CosmicBreakpoint")).let {
        it.loadCosmicFile()
        println("Loaded CosmicCBreakpoint data row count = ${it.getNodeCount()}")
    }
}


