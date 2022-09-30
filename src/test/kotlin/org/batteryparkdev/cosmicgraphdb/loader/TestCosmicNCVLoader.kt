package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicNCV

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else  "./data/CosmicNCV.tsv"
    TestCoreModelLoader(
        CosmicNCV, filename, listOf("CosmicNCV")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic NCV data row count = ${it.getNodeCount()}")
    }
}