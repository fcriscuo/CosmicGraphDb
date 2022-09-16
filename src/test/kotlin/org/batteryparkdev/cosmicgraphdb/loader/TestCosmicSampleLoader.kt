package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicSample

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/CosmicSample.tsv"
    TestCoreModelLoader(
        CosmicSample, filename, listOf("CosmicSample","CosmicTumor", "CosmicPatient")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Sample, Cosmic Tumor, & Cosmic Patient data; row count = ${it.getNodeCount()}")
    }
}