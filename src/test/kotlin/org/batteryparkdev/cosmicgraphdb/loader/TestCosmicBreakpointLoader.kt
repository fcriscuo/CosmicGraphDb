package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicBreakpoint

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/CosmicBreakpointsExport.tsv"
    TestCoreModelLoader(CosmicBreakpoint, filename, listOf("CosmicBreakpoint")).let {
        it.testLoadData()
        println("Loaded Cosmic Breakpoint mutations data row count = ${it.getNodeCount()}")
    }
}


