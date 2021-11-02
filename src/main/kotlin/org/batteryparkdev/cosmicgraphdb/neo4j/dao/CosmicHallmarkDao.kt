package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicHallmark
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService

private val logger: FluentLogger = FluentLogger.forEnclosingClass()

fun createCosmicGeneRelationship(hallmark: CosmicHallmark) {
    when (cancerGeneNameLoaded(hallmark.geneSymbol)) {
        true -> Neo4jConnectionService.executeCypherCommand(
            "MATCH (ch:CosmicHallmark), (cg:CosmicGene) WHERE " +
                    " ch.hallmark_id=${hallmark.hallmarkId} AND cg.gene_symbol = " +
                    "\"${hallmark.geneSymbol}\" " +
                    " MERGE (cg) - [r:HAS_HALLMARK] -> (ch)"
        )
        false -> logger.atWarning().log("Hallmark gene symbol: ${hallmark.geneSymbol} " +
                " not registered as a CosmicGene node")
    }
}

fun loadCosmicHallmark(hallmark: CosmicHallmark): Int =
    Neo4jConnectionService.executeCypherCommand(
        "MERGE (ch:CosmicHallmark{hallmark_id: ${hallmark.hallmarkId}}) " +
                "SET ch.gene_symbol =\"${hallmark.geneSymbol}\", " +
                " ch.cell_type = \"${hallmark.cellType}\", " +
                " ch.pubmed_id = \"${hallmark.pubmedId}\"," +
                " ch.hallmark = \"${hallmark.hallmark}\", " +
                " ch.impact = \"${hallmark.impact}\", " +
                " ch.description = \"${hallmark.description}\" " +
                " RETURN ch.hallmark_id"
    ).toInt()

fun addCosmicHallmarkLabel(id: Int, label: String) {
    val labelExistsQuery = "MERGE (ch:CosmicHallmark{hallmark_id:$id}) " +
            "RETURN apoc.label.exists(ch, \"$label\") AS output;"
    when (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
        true ->Neo4jConnectionService.executeCypherCommand(
            "MATCH (ch:CosmicHallmark{hallmark_id:$id}) " +
                    "CALL apoc.create.addLabels(ch,[\"$label\"]) YIELD node RETURN node"
        )
        false -> logger.atWarning().log("Hallmark label: $label already assigned to hallmark id ${id}")
    }
}