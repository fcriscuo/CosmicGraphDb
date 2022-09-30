package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicResistanceMutation

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else  "./data/CosmicResistanceMutations.tsv"
    TestCoreModelLoader(
        CosmicResistanceMutation, filename, listOf("CosmicResistanceMutation")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Resistance Mutation data row count = ${it.getNodeCount()}")
    }
}
