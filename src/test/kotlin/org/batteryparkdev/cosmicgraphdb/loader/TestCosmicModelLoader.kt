package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.neo4j.service.Neo4jUtils

class TestCosmicModelLoader {

    fun loadCosmicFiles(filenameList: List<String>){
        deleteCosmicModelNodes()
       filenameList.forEach { filename -> CosmicModelLoader(filename).loadCosmicFile() }
    }

    private fun deleteCosmicModelNodes(){
        Neo4jUtils.detachAndDeleteNodesByName("CosmicHGNC")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicHallmark")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicGene")

    }
}

fun main() {
    val filenames = listOf<String>("cancer_gene_census.csv", "Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv",
        "CosmicHGNC.tsv")
    TestCosmicModelLoader().loadCosmicFiles(filenames)
    println("FINIS.....")
}