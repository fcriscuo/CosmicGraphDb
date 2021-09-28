package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicClassification
import org.batteryparkdev.cosmicgraphdb.io.CsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import java.nio.file.Paths

object CosmicClassificationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicClassification(cosmicClassification: CosmicClassification){
        val id = loadCosmicClassification(cosmicClassification)
        loadCosmicSites(cosmicClassification)
        createCosmicTypeRelationships(cosmicClassification)
        logger.atInfo().log("Loaded CosmicClassification id = ${cosmicClassification.cosmicPhenotypeId}")
    }

   private fun loadCosmicClassification(cosmicClassification: CosmicClassification): String =
        Neo4jConnectionService.executeCypherCommand(
            "MERGE (cc:CosmicClassification{phenotype_id:\"${cosmicClassification.cosmicPhenotypeId}\"}) " +
                    "SET cc.nci_code =\"${cosmicClassification.nciCode}\", cc.efo_url= \"${cosmicClassification.efoUrl}\"" +
                    " RETURN cc.phenotype_id"
        )

   private fun loadCosmicSites(cosmicClassification: CosmicClassification) {
        CosmicTypeLoader.processCosmicTypeNode(cosmicClassification.siteType)
        CosmicTypeLoader.processCosmicTypeNode(cosmicClassification.histologyType)
        if (cosmicClassification.cosmicSiteType != null) {
            CosmicTypeLoader.processCosmicTypeNode(cosmicClassification.cosmicSiteType)
        }
        if (cosmicClassification.cosmicHistologyType != null) {
            CosmicTypeLoader.processCosmicTypeNode(cosmicClassification.cosmicHistologyType)
        }
    }

    private fun createCosmicTypeRelationships(cosmicClassification: CosmicClassification) {
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (cc:CosmicClassification), (ct:CosmicType) WHERE cc.phenotype_id = " +
                    " \"${cosmicClassification.cosmicPhenotypeId}\" AND ct.type_id = " +
                    "${cosmicClassification.siteType.generateIdentifier()} MERGE " +
                    "(cc) - [r:HAS_SITE] -> (ct)"
        )
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (cc:CosmicClassification), (ct:CosmicType) WHERE cc.phenotype_id = " +
                    " \"${cosmicClassification.cosmicPhenotypeId}\" AND ct.type_id = " +
                    "${cosmicClassification.histologyType.generateIdentifier()} MERGE " +
                    "(cc) - [r:HAS_HISTOLOGY] -> (ct)"
        )
        if (cosmicClassification.cosmicSiteType != null) {
            Neo4jConnectionService.executeCypherCommand(
                "MATCH (cc:CosmicClassification), (ct:CosmicType) WHERE cc.phenotype_id = " +
                        " \"${cosmicClassification.cosmicPhenotypeId}\" AND ct.type_id = " +
                        "${cosmicClassification.cosmicSiteType.generateIdentifier()} MERGE " +
                        "(cc) - [r:HAS_COSMIC_SITE] -> (ct)"
            )
        }
        if (cosmicClassification.cosmicHistologyType != null) {
            Neo4jConnectionService.executeCypherCommand(
                "MATCH (cc:CosmicClassification), (ct:CosmicType) WHERE cc.phenotype_id = " +
                        " \"${cosmicClassification.cosmicPhenotypeId}\" AND ct.type_id = " +
                        "${cosmicClassification.cosmicHistologyType.generateIdentifier()} MERGE " +
                        "(cc) - [r:HAS_COSMIC_HISTOLOGY] -> (ct)"
            )
        }
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