package org.batteryparkdev.cosmicgraphdb.app

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.*
import org.batteryparkdev.cosmicgraphdb.io.CsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
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
    private val cosmicGeneFile = fileDirectory +
        DatafilePropertiesService.resolvePropertyAsString("file.cosmic.gene")
    private val cosmicClassicationFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.classification")
    private val cosmicSampleFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.sample")
    private val cosmicMutationExportCensusFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.mutant.export.census")
    private val cosmicHallmarkFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.gene.census.hallmarks.of.cancer")

    fun loadCosmicDatabase() {
        // load order is import for establishing parent to child relationships
        loadCosmicGeneNodes()
        loadCosmicHallmarkNodes()
        loadCosmicClassificationGeneNodes()
        loadCosmicSampleNodes()
        loadCosmicMutationNodes()
        loadCosmicTumorNodes()
        logger.atInfo().log("All currently supported COSMIC data has been loaded into Neo4j")
    }

    private fun loadCosmicHallmarkNodes(){
        detachAndDeleteNodesByName("CosmicHallmark")
        val path = Paths.get(cosmicHallmarkFile)
        logger.atInfo().log("Loading cancer hallmarks using file: ${path.fileName}")
        var recordCount = 0
        TsvRecordSequenceSupplier(path).get().chunked(500)
            .forEach { it ->
                it.stream()
                    .map { CosmicHallmark.parseCsvRecord(it) }
                    .forEach { hallmark ->
                        CosmicHallmarkLoader.processCosmicHallmark(hallmark)
                        recordCount += 1
                    }
            }
        logger.atInfo().log("Cosmic Hallmark record count = $recordCount")
    }

    private fun loadCosmicTumorNodes() {
        detachAndDeleteNodesByName("CosmicTumor")
        val path = Paths.get(cosmicMutationExportCensusFile)   // same file as mutations
        logger.atInfo().log("Loading tumors using CosmicMutationExportCensus file ${path.fileName}")
        var recordCount = 0
        TsvRecordSequenceSupplier(path).get().chunked(500)
            .forEach { it ->
                it.stream()
                    .map { CosmicTumor.parseCsvRecord(it) }
                    .forEach { tumor ->
                        CosmicTumorLoader.processCosmicTumor(tumor)
                        recordCount += 1
                    }
            }
        logger.atInfo().log("Cosmic Tumor record count = $recordCount")
    }

    private fun loadCosmicMutationNodes() {
        detachAndDeleteNodesByName("CosmicMutation")
        val path = Paths.get(cosmicMutationExportCensusFile)
        logger.atInfo().log("Loading mutations using CosmicMutationExportCensus file ${path.fileName}")
        var recordCount = 0
        TsvRecordSequenceSupplier(path).get().chunked(500)
            .forEach { it ->
                it.stream()
                    .map { CosmicMutation.parseCsvRecord(it) }
                    .forEach { mutation ->
                        CosmicMutationLoader.processCosmicMutation(mutation)
                        recordCount += 1
                    }
            }
        logger.atInfo().log("Cosmic Mutation record count = $recordCount")
    }

    private fun loadCosmicSampleNodes() {
        detachAndDeleteNodesByName("CosmicSample")
        val path = Paths.get(cosmicSampleFile)
        logger.atInfo().log("Loading CosmicSample file ${path.fileName}")
        var recordCount = 0
        TsvRecordSequenceSupplier(path).get().chunked(500)
            .forEach { it ->
                it.stream()
                    .map { CosmicSample.parseCsvRecord(it) }
                    .forEach { sample ->
                        CosmicSampleLoader.processCosmicSampleNode(sample)
                        recordCount += 1
                    }
            }
        logger.atInfo().log("Cosmic Sample record count = $recordCount")
    }

    private fun loadCosmicClassificationGeneNodes() {
        detachAndDeleteNodesByName("CosmicClassification")
        val path = Paths.get(cosmicClassicationFile)
        logger.atInfo().log("Loading CosmicClassification file ${path.fileName}")
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
        logger.atInfo().log("Cosmic Classification record count = $recordCount")
    }

    private fun loadCosmicGeneNodes() {
        detachAndDeleteNodesByName("CosmicGene")
        val path = Paths.get(cosmicGeneFile)
        logger.atInfo().log("Processing Cosmic Gene Census file ${path.fileName}")
        var recordCount = 0
        CsvRecordSequenceSupplier(path).get().chunked(500)
            .forEach { it ->
                it.stream()
                    .map { CosmicGeneCensus.parseCsvRecord(it) }
                    .forEach {
                        CosmicGeneLoader.processCosmicGeneNode(it)
                        recordCount += 1
                    }
            }
        logger.atInfo().log("Cosmic Gene Census record count = $recordCount")
    }

    // detach and delete specified nodes in database
    fun detachAndDeleteNodesByName(nodeName: String) {
        val beforeCount = Neo4jConnectionService.executeCypherCommand(
            "MATCH (n: $nodeName) RETURN COUNT (n)"
        )
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (n: $nodeName) DETACH DELETE (n);")
        val afterCount = Neo4jConnectionService.executeCypherCommand(
            "MATCH (n: $nodeName) RETURN COUNT (n)"
        )
        logger.atInfo().log("Deleted $nodeName nodes, before count=${beforeCount.toString()}" +
                "  after count=$afterCount")
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

