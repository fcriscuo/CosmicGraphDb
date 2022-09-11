package org.batteryparkdev.cosmicgraphdb.model

fun main (args: Array<String>) {
    // the patient data are in the sample file
    val filename = if (args.isNotEmpty()) args[0] else
        "/Volumes/SSD870/COSMIC_rel96/sample/CosmicSample.tsv"
    TestCoreModel(CosmicPatient.Companion).loadModels(filename)
}
