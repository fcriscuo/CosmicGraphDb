package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader

fun main (args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/CosmicSample.tsv"
    CoreModelLoader(CosmicSample.Companion).loadDataFile(filename)
}
