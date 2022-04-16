package org.batteryparkdev.cosmicgraphdb.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.model.CosmicClassification
import org.batteryparkdev.neo4j.service.Neo4jConnectionService


/*
Functions responsible for interactions with the Neo4j database
involving CancerClassification data
 */
private val logger: FluentLogger = FluentLogger.forEnclosingClass()

fun loadCosmicClassification(classification: CosmicClassification): Int {


    val cypher = classification.generateMergeCypher()
        .plus(classification.siteType.generateMergeCypher())
        .plus(classification.siteType.generateParentRelationshipCypher(classification.nodeName))
        .plus(classification.histologyType.generateMergeCypher())
        .plus(classification.histologyType.generateParentRelationshipCypher(classification.nodeName))
        .plus(" RETURN ${classification.nodeName}")
    println(cypher)
    val node = Neo4jConnectionService.executeCypherCommand(cypher)
    return classification.resolveClassificationId()
}


