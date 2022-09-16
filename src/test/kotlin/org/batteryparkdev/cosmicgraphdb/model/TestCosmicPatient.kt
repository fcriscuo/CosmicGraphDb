package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    // the patient data are in the sample file
    val filename = if (args.isNotEmpty()) args[0] else
        "./data/CosmicSample.tsv"
    TestCoreModel(CosmicPatient.Companion).loadModels(filename)
}
