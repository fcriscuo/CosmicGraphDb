package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader

class TestCosmicGeneCensus {
    private val LIMIT = 20L
    /*

    n.b. file name specification must be full path since it is resolved by Neo4j server
     */
    fun parseCosmicGeneCensusFile(filename: String): Int {
        // limit the number of records processed

        var recordCount = 0
        ApocFileReader.processDelimitedFile(filename)
            .stream().limit(LIMIT)
            .map { record -> record.get("map") }
            .map { CosmicGeneCensus.parseValueMap(it) }
            .forEach { gene->
                println(gene.generateCosmicGeneCypher())
                recordCount += 1
            }
        return recordCount
    }
}
fun main() {
    val recordCount =
        TestCosmicGeneCensus().
            parseCosmicGeneCensusFile("/Volumes/SSD870/COSMIC_rel95/sample/cancer_gene_census.csv")
    println("Breakpoint record count = $recordCount")
}