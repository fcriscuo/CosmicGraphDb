package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicBreakpoint

fun main () {
    val filename = "./data/CosmicBreakpointsExport.tsv"
    TestCoreModelLoader(CosmicBreakpoint, filename, listOf("CosmicBreakpoint")).let {
        it.testLoadData()
        println("Loaded Cosmic Breakpoint mutations data row count = ${it.getNodeCount()}")
    }
}


