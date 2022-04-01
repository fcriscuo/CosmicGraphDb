package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader

class TestCosmicClassification {
    /*
    Test function to parse 100 records from the Cosmic classification.csv file
     */

    fun parseClassificationFile(filename: String) {
        val LIMIT = 100L
//        val cypher = "CALL apoc.load.csv(\"$filename\") " +
//                "YIELD lineNo, map RETURN map;"
//        val records = Neo4jConnectionService.executeCypherQuery(cypher);
//        records.stream()
        ApocFileReader.processDelimitedFile(filename).stream()
            .limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicClassification.parseValueMap(it) }
            .forEach {
                println(
                    "CosmicClassification: ${it.resolveClassificationId()} " +
                            " NCIcode: ${it.nciCode}" +
                            " Primary Site: ${it.siteType.primary} " +
                            " Histology: ${it.histologyType.primary}"
                )
            }
    }
}

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0]
    else "/Volumes/SSD870/COSMIC_rel95/sample/classification.csv"
    val apoc = TestCosmicClassification()
    apoc.parseClassificationFile(filename)
}