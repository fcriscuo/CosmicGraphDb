package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader

class TestCosmicCompleteExpression {

    fun parseCosmicCompleteExpressionFile (filename:String){
        val LIMIT = 100L
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map{CosmicCompleteGeneExpression.parseValueMap(it)}
            .forEach { println("CosmicCompleteExpression: ${it.generateCompleteGeneExpressionCypher()}") }
    }
}
fun main (args:Array<String>) {
    val filename = if (args.isNotEmpty()) args[0]
    else "/Volumes/SSD870/COSMIC_rel95/sample/CosmicCompleteGeneExpression.tsv"
    TestCosmicCompleteExpression().parseCosmicCompleteExpressionFile(filename)
}
