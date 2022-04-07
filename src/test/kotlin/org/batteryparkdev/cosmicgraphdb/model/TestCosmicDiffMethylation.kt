package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader

class TestCosmicDiffMethylation {
    fun parseCosmicCompleteDifferentialMethylation(filename: String) {
        val LIMIT = 100L
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicDiffMethylation.parseValueMap(it) }
            .forEach { println("CosmicCompleteDiffMethlation: ${it.generateDiffMethylationCypher()}") }
    }
}

fun main (args:Array<String>) {
    val filename = if (args.isNotEmpty()) args[0]
    else "/Volumes/SSD870/COSMIC_rel95/sample/CosmicCompleteDifferentialMethylation.tsv"
}