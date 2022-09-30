package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicFusion

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else  "./data/CosmicFusionExport.tsv"
    TestCoreModelLoader(
        CosmicFusion, filename, listOf("CosmicFusion")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Fusion data row count = ${it.getNodeCount()}")
    }
}

