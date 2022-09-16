package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicDiffMethylation

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else  "./data/CosmicCompleteDifferentialMethylation.tsv"
    TestCoreModelLoader(
        CosmicDiffMethylation, filename, listOf("CosmicDiffMethylation")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Complete Differential Methylation data row count = ${it.getNodeCount()}")
    }
}

