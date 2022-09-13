package org.batteryparkdev.cosmicgraphdb.loader

import arrow.core.NonEmptyList
import org.batteryparkdev.genomicgraphcore.common.CoreModelLoader
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils


class TestCosmicLoader(val filename: String,val  nodeLabelList: NonEmptyList<String>) {

    fun deleteCosmicNode(): Unit =
        nodeLabelList.forEach { label -> Neo4jUtils.detachAndDeleteNodesByName(label) }

     fun getNodeCount(): Int =
         Neo4jConnectionService.executeCypherCommand(
             "MATCH (n:${nodeLabelList[0].formatNeo4jPropertyValue()}) RETURN COUNT(n)").toInt()

    fun loadCosmicFile( ): Unit {
        deleteCosmicNode()
        CosmicModelLoader(filename).loadCosmicFile()
        return
    }

}