package org.batteryparkdev.cosmicgraphdb.poc

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils

class PubMedQueryPoc {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()
}

fun main() {
    Neo4jUtils.resolvePlaceholderPubMedArticleNodes().forEach {
        println(it)
    }

}

