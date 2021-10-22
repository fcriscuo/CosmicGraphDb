package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicTumor
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.*
import java.nio.file.Paths

object CosmicTumorLoader

{
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicTumor(cosmicTumor: CosmicTumor) {
        if (!cosmicTumorIdLoaded(cosmicTumor.tumorId)) {
            loadCosmicTumor(cosmicTumor)
            CosmicTypeDao.processCosmicTypeNode(cosmicTumor.site)
            CosmicTypeDao.processCosmicTypeNode(cosmicTumor.histology)
        }
        // tumor -> sample relationship
        createCosmicSampleRelationship(cosmicTumor)
        // tumor -> mutation relationship
        createCosmicMutationRelationship(cosmicTumor)
        // tumor -> pubmed relationship
        createPubMedRelationship(cosmicTumor)
    }

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