package org.batteryparkdev.pubmedref.neo4j

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService

/*
A collection of Neo4j database constraint definitions in Cypher
These constraints should be defined prior to loading the database with
any initial data.
 */
val constraints = listOf<String>(
    "CREATE CONSTRAINT unique_classification_id IF NOT EXISTS ON (c:CosmicClassification) ASSERT c.cosmic_phenotype_id IS UNIQUE",
    "CREATE CONSTRAINT unique_classification_type_id IF NOT EXISTS ON (t:ClassificationType) ASSERT t.type_id IS UNIQUE"
)

val logger: FluentLogger = FluentLogger.forEnclosingClass();

fun defineConstraints() {
    constraints.forEach {
        Neo4jConnectionService.defineDatabaseConstraint(it)
        logger.atInfo().log("Constraint: $it  has been defined")
    }
}

// stand-alone invocation
fun main(){
    defineConstraints()
}