package org.batteryparkdev.cosmicgraphdb.app

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.*
import org.batteryparkdev.cosmicgraphdb.io.CsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.neo4j.loader.*
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService
import java.nio.file.Paths

/*
Primary COSMIC data loader
Loads COSMIC data files located in a directory either specified as a program argument or
defined in the datafiles.properties file (cosmic.data.directory)
n.b. This application deletes all existing COSMIC nodes and relationships from the
     Neo4j database
 */

class CosmicDatabaseLoader(fileDirectory: String) {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();
    private val cosmicHGNCFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.hgnc")
    private val cosmicCompleteCNAFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.complete.cna")
    private val cosmicDiffMethylationFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.complete.differential.methylation")
    private val cosmicGeneExpressionFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.complete.gene.expression")
    private val cosmicGeneCensusFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.gene.census")
    private val cosmicClassificationFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.classification")
    private val cosmicSampleFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.sample")
    private val cosmicMutationExportCensusFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.mutant.export.census")
    private val cosmicHallmarkFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.gene.census.hallmarks.of.cancer")

    private val nodeNameList = listOf<String>(
        "CosmicHallmark", "CosmicTumor", "CosmicMutation",
        "CosmicSample", "CosmicClassification", "CosmicGene", "CosmicType",
        "CosmicCompleteDNA", "CosmicGeneExpression", "CosmicDiffMethylation"
    )

    /*
    Function to delete all Cosmic-related nodes and relationships
    prior to reloading the database
    n.b. PubMed-related nodes are NOT deleted
     */
    private fun deleteCosmicNodes() {
        nodeNameList.forEach { nodeName -> Neo4jUtils.detachAndDeleteNodesByName(nodeName) }
    }

    fun loadCosmicDatabase() {
        // load order is import for establishing parent to child relationships
        deleteCosmicNodes()
        CosmicGeneCensusLoader.loadCosmicGeneCensusData(cosmicGeneCensusFile)
        CosmicHallmarkLoader.processCosmicHallmarkData(cosmicHallmarkFile)
        CosmicClassificationLoader.loadCosmicClassificationData(cosmicClassificationFile)
        CosmicSampleLoader.processCosmicSampleData(cosmicSampleFile)
        // mutation and tumor data are loaded from the same file
        //TODO: refactor to use a common channel for CSVRecords
        CosmicMutationLoader.processCosmicMutationData(cosmicMutationExportCensusFile)
        CosmicTumorLoader.processCosmicTumorData(cosmicMutationExportCensusFile)
        CosmicCompleteCNALoader.loadCosmicCompleteCNAData(cosmicCompleteCNAFile)
        CosmicDiffMethylationLoader.loadCosmicDiffMethylationData(cosmicDiffMethylationFile)
        CosmicGeneExpressionLoader.loadCosmicCompleteGeneExpressionData(cosmicGeneExpressionFile)
        logger.atInfo().log("All currently supported COSMIC data has been loaded into Neo4j")
    }
}
    fun main(args: Array<String>) {
        val fileDirectory =
            when (args.size > 0) {
                true -> args[0]
                false -> DatafilePropertiesService.resolvePropertyAsString("cosmic.data.directory")
            }
        println("WARNING: Invoking this application will delete all COSMIC data from the database")
        println("There will be a 20 second delay period to cancel this execution (CTRL-C) if this is not your intent")
        Thread.sleep(20_000L)
        println("Cosmic data will now be loaded from: $fileDirectory")
        if (fileDirectory.isNotEmpty()) {
            CosmicDatabaseLoader(fileDirectory).loadCosmicDatabase()
        }
    }

