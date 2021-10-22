package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createCosmicGeneRelationship
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicMutation
import java.nio.file.Paths

/*
Responsible for loading data from a CosmicMutation model instance into the Neo4j database
Creates a  CosmicMutation -> CosmicGene relationship
 */

object CosmicMutationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicMutation(cosmicMutation: CosmicMutation) {
        val id = loadCosmicMutation(cosmicMutation)
        createCosmicGeneRelationship(cosmicMutation.geneName, id)
    }

}
fun main() {
    val path = Paths.get("./data/sample_CosmicMutantExportCensus.tsv")
    println("Processing cosmic mutation file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicMutation.parseCsvRecord(it) }
                .forEach { mut ->
                   CosmicMutationLoader.processCosmicMutation(mut)
                    println("Loaded mutation id ${mut.mutationId}  gene symbol ${mut.geneName}")
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}