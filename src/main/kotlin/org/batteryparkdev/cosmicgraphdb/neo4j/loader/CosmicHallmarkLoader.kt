package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicHallmark
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import java.nio.file.Paths


object CosmicHallmarkLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicHallmark(hallmark: CosmicHallmark) {
        loadCosmicHallmark(hallmark)
        addCosmicHallmarkLabel(hallmark.hallmarkId, hallmark.hallmark)
        createCosmicGeneRelationship(hallmark)
    }

    private fun createCosmicGeneRelationship(hallmark: CosmicHallmark) {
        when (CosmicGeneLoader.cancerGeneNameLoaded(hallmark.geneSymbol)) {
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

    private fun loadCosmicHallmark(hallmark: CosmicHallmark): Int =
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

    private fun addCosmicHallmarkLabel(id: Int, label: String) {
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
}

fun main() {
    val path = Paths.get("./data/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    println("Processing cosmic hallmarks file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicHallmark.parseCsvRecord(it) }
                .forEach { hallmark ->
                    CosmicHallmarkLoader.processCosmicHallmark(hallmark)
                    println(
                        "Loaded hallmark id: ${hallmark.hallmarkId}  " +
                                " hallmark: ${hallmark.hallmark}" +
                                "\n     description: ${hallmark.description}"
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}