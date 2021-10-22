package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicClassification
import org.batteryparkdev.cosmicgraphdb.io.CsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.CosmicTypeDao
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.createCosmicTypeRelationships
import org.batteryparkdev.cosmicgraphdb.neo4j.dao.loadCosmicClassification
import java.nio.file.Paths

object CosmicClassificationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicClassification(cosmicClassification: CosmicClassification){
        val id = loadCosmicClassification(cosmicClassification)
        loadCosmicSites(cosmicClassification)
        createCosmicTypeRelationships(cosmicClassification)
        logger.atInfo().log("Loaded CosmicClassification id = ${cosmicClassification.cosmicPhenotypeId}")
    }

   private fun loadCosmicSites(cosmicClassification: CosmicClassification) {
        CosmicTypeDao.processCosmicTypeNode(cosmicClassification.siteType)
        CosmicTypeDao.processCosmicTypeNode(cosmicClassification.histologyType)
       CosmicTypeDao.processCosmicTypeNode(cosmicClassification.cosmicSiteType)
       CosmicTypeDao.processCosmicTypeNode(cosmicClassification.cosmicHistologyType)
    }
}
fun main() {
    val path = Paths.get("./data/classification.csv")
    println("Loading CosmicClassification file ${path.fileName}")
    var recordCount = 0
    CsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicClassification.parseCsvRecord(it) }
                .forEach { cc ->
                   CosmicClassificationLoader.processCosmicClassification(cc)
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}