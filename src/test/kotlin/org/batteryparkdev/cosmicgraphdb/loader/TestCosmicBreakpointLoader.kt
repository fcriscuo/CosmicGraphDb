package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.TestCosmicBreakpoint

class TestCosmicBreakpointLoader {
    fun  processBreakpointFile(filename:String)  =
        CosmicBreakpointLoader.loadCosmicBreakpointData(filename)
}
fun main() {
    val recordCount =
        TestCosmicBreakpointLoader().processBreakpointFile("/Volumes/SSD870/COSMIC_rel95/sample/CosmicBreakpointsExport.tsv")
    println("Test finished")
}

