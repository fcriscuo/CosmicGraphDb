package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicTumor
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import java.nio.file.Paths

object CosmicTumorLoader

{
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicTumor(cosmicTumor: CosmicTumor) {
        if (!cosmicTumorLoaded(cosmicTumor.tumorId)) {
            loadCosmicTumor(cosmicTumor)
            CosmicTypeLoader.processCosmicTypeNode(cosmicTumor.site)
            CosmicTypeLoader.processCosmicTypeNode(cosmicTumor.histology)
        }
        // tumor -> sample relationship
        createCosmicSampleRelationship(cosmicTumor)
        // tumor -> mutation relationship
        createCosmicMutationRelationship(cosmicTumor)
    }

    private fun createCosmicMutationRelationship(cosmicTumor: CosmicTumor) =
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (ct:CosmicTumor), (cm:CosmicMutation) " +
                    " WHERE ct.tumor_id = ${cosmicTumor.tumorId} AND " +
                    " cm.mutation_id = ${cosmicTumor.mutationId} MERGE " +
                    " (ct) -[r:HAS_MUTATION] ->(cm)"
        )


    private fun createCosmicSampleRelationship(cosmicTumor: CosmicTumor) =
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (ct:CosmicTumor), (cs:CosmicSample) " +
                    " WHERE ct.tumor_id = ${cosmicTumor.tumorId} AND " +
                    " cs.sample_id = ${cosmicTumor.sampleId} MERGE " +
                    " (ct) -[r:HAS_SAMPLE] -> (cs)"
        )


    private fun loadCosmicTumor(cosmicTumor: CosmicTumor):Int  =
        Neo4jConnectionService.executeCypherCommand(
            "MERGE (ct:CosmicTumor{tumor_id: ${cosmicTumor.tumorId}}) " +
                    "SET ct.genome_wide_screen = ${cosmicTumor.genomeWideScreen}," +
                    " ct.pubmed_id = \"${cosmicTumor.pubmedId}\", ct.study_id = \"${cosmicTumor.studyId}\", " +
                    " ct.sample_type =\"${cosmicTumor.sampleType}\", ct.tumor_origin = \"${cosmicTumor.tumorOrigin}\", " +
                    " ct.age = ${cosmicTumor.age}  RETURN ct.tumor_id"
        ).toInt()

    /*
    Predicate to determine if a CancerTumor node with the specified id has
    already been loaded
     */
    fun cosmicTumorLoaded(tumorId: Int): Boolean =
        Neo4jUtils.nodeLoadedPredicate(
            "OPTIONAL MATCH (ct:CosmicTumor{tumor_id: $tumorId }) " +
                    " RETURN ct IS NOT NULL AS PREDICATE"
        )
}
fun main() {
    val path = Paths.get("./data/sample_CosmicMutantExportCensus.tsv")
    println("Processing cosmic tumorn file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicTumor.parseCsvRecord(it) }
                .forEach { tumor ->
                    CosmicTumorLoader.processCosmicTumor(tumor)
                    println("Loaded tumor id ${tumor.tumorId} primary site: ${tumor.site.primary}")
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}