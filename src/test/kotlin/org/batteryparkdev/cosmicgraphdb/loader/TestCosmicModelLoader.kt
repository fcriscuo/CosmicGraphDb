package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.model.CosmicHallmark
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils


class TestCosmicModelLoader {

    fun loadCosmicFiles(creatorList: List<Pair<CoreModelCreator,String>>){
        deleteCosmicModelNodes()
       creatorList.forEach { pair -> CoreModelLoader(pair.first).loadDataFile(pair.second) }
    }

    private fun deleteCosmicModelNodes(){
        Neo4jUtils.detachAndDeleteNodesByName("CosmicHallmark")
        Neo4jUtils.detachAndDeleteNodesByName("CosmicGene")
    }
}

fun main() {
    if (Neo4jConnectionService.isSampleContext()) {
        val models = listOf<Pair<CoreModelCreator, String>>(
            Pair(CosmicGeneCensus, "./data/cancer_gene_census.csv"),
            Pair(CosmicHallmark, "./data/Cancer_Gene_Census_Hallmarks_Of_Cancer")
        )
        TestCosmicModelLoader().loadCosmicFiles(models)
    } else {
        println("ERROR - Tests can only be run against the sample database")
        println("Edit the neo4j.database setting in the neo4j.config file in the ~/.genomicapps directory")
    }
    println("FINIS.....")
}
