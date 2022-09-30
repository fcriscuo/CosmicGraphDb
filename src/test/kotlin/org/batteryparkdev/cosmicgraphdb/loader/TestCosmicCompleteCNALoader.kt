package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteCNA

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else  "./data/CosmicCompleteCNA.tsv"
    TestCoreModelLoader(
        CosmicCompleteCNA, filename, listOf("CosmicCompleteCNA")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Complete CNA data row count = ${it.getNodeCount()}")
    }
}


