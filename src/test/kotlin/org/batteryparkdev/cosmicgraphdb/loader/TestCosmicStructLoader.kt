package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicStruct

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/CosmicStructExport.tsv"
    TestCoreModelLoader(
        CosmicStruct, filename, listOf("CosmicStruct")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Struct mutation data row count = ${it.getNodeCount()}")
    }
}