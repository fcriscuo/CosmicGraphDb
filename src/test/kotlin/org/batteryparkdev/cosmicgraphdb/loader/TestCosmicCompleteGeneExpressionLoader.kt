package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicCompleteGeneExpression

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else  "./data/CosmicCompleteGeneExpression.tsv"
    TestCoreModelLoader(
        CosmicCompleteGeneExpression, filename, listOf("CompleteGeneExpression")
    ).let {
        it.testLoadData()
        println("Loaded Cosmic Complete Gene Expression data row count = ${it.getNodeCount()}")
    }
}
