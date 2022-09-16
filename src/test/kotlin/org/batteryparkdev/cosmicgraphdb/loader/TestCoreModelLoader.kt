package org.batteryparkdev.cosmicgraphdb.loader

import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

class TestCoreModelLoader (private val creator: CoreModelCreator, private val filename: String,
                           private val nodeLabelList: List<String>){

    fun getNodeCount(): Int =
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (n:${nodeLabelList[0].formatNeo4jPropertyValue()}) RETURN COUNT(n)").toInt()

    private fun deleteCosmicNodes(): Unit =
        nodeLabelList.forEach{ label -> Neo4jUtils.detachAndDeleteNodesByName(label) }

    fun testLoadData() {
        if(Neo4jConnectionService.isSampleContext()) {
            deleteCosmicNodes()
            println("Loading data from sample file: $filename")
            CoreModelLoader(creator).loadDataFile(filename)
        } else {
            println("ERROR: Data loading tests can only be run against the sample Neo4j database")
        }
    }
}