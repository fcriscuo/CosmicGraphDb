package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicHallmark
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.pubmed.dao.PubMedArticleDao
import org.batteryparkdev.cosmicgraphdb.pubmed.model.PubMedIdentifier

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

fun createPubMedRelationship(hallmark: CosmicHallmark){
    val identifier = PubMedIdentifier(hallmark.pubmedId.toInt(),0,"CosmicArticle")
    PubMedArticleDao.createPlaceholderNode(identifier)
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (ch:CosmicHallmark), (pma:PubMedArticle) WHERE " +
                " ch.hallmark_id=${hallmark.hallmarkId}  AND pma.pubmedId =" +
                " ${hallmark.pubmedId} MERGE (ch) -[r:HAS_COSMIC_ARTICLE] -> (pma) "
    )

}

fun loadCosmicHallmark(hallmark: CosmicHallmark): Int =
    Neo4jConnectionService.executeCypherCommand(
        "MERGE (ch:CosmicHallmark{hallmark_id: ${hallmark.hallmarkId}}) " +
                "SET ch += {gene_symbol = ${Neo4jUtils.formatQuotedString(hallmark.geneSymbol)}, " +
                " cell_type = \"${Neo4jUtils.formatQuotedString(hallmark.cellType)}\", " +
                " pubmed_id = \"${hallmark.pubmedId}\"," +
                " hallmark = \"${hallmark.hallmark}\", " +
                " impact = \"${hallmark.impact}\", " +
                " description = \"${hallmark.description}\" }" +
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