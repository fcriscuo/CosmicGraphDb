package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicClassification

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/classification.csv"
    TestCoreModelLoader(
        CosmicClassification, filename, listOf("CosmicClassification" )).let {
        it.testLoadData()
        println("Loaded Cosmic Classifications data row count = ${it.getNodeCount()}")
    }
}
