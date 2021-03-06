package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicClassification
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService

/*
Functions responsible for interactions with the Neo4j database
involving CancerClassification data
 */
private val logger: FluentLogger = FluentLogger.forEnclosingClass()

fun loadCosmicClassification(cosmicClassification: CosmicClassification): String =
    Neo4jConnectionService.executeCypherCommand(
        "MERGE (cc:CosmicClassification{phenotype_id:\"${cosmicClassification.cosmicPhenotypeId}\"}) " +
                "SET cc.nci_code =\"${cosmicClassification.nciCode}\", cc.efo_url= \"${cosmicClassification.efoUrl}\"" +
                " RETURN cc.phenotype_id"
    )

fun createCosmicTypeRelationships(cosmicClassification: CosmicClassification) {
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cc:CosmicClassification), (ct:CosmicType) WHERE cc.phenotype_id = " +
                " \"${cosmicClassification.cosmicPhenotypeId}\" AND ct.type_id = " +
                "${cosmicClassification.siteType.generateIdentifier()} MERGE " +
                "(cc) - [r:HAS_SITE] -> (ct)"
    )
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cc:CosmicClassification), (ct:CosmicType) WHERE cc.phenotype_id = " +
                " \"${cosmicClassification.cosmicPhenotypeId}\" AND ct.type_id = " +
                "${cosmicClassification.histologyType.generateIdentifier()} MERGE " +
                "(cc) - [r:HAS_HISTOLOGY] -> (ct)"
    )
    if (cosmicClassification.cosmicSiteType != null) {
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (cc:CosmicClassification), (ct:CosmicType) WHERE cc.phenotype_id = " +
                    " \"${cosmicClassification.cosmicPhenotypeId}\" AND ct.type_id = " +
                    "${cosmicClassification.cosmicSiteType.generateIdentifier()} MERGE " +
                    "(cc) - [r:HAS_COSMIC_SITE] -> (ct)"
        )
    }
    if (cosmicClassification.cosmicHistologyType != null) {
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (cc:CosmicClassification), (ct:CosmicType) WHERE cc.phenotype_id = " +
                    " \"${cosmicClassification.cosmicPhenotypeId}\" AND ct.type_id = " +
                    "${cosmicClassification.cosmicHistologyType.generateIdentifier()} MERGE " +
                    "(cc) - [r:HAS_COSMIC_HISTOLOGY] -> (ct)"
        )
    }
}