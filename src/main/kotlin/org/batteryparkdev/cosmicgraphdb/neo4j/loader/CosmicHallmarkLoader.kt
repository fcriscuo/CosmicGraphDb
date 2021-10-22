package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicHallmark
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.addCosmicHallmarkLabel
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createCosmicGeneRelationship
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicHallmark
import java.nio.file.Paths

object CosmicHallmarkLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    fun processCosmicHallmark(hallmark: CosmicHallmark) {
        loadCosmicHallmark(hallmark)
        addCosmicHallmarkLabel(hallmark.hallmarkId, hallmark.hallmark)
        createCosmicGeneRelationship(hallmark)
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