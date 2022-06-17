package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.model.*
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService
import org.batteryparkdev.io.CSVRecordSupplier
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.neo4j.driver.Value
import java.nio.file.Paths
import kotlin.streams.asSequence

class CosmicModelLoader( val filename: String, val runmode:String = "sample") {

    private val filenameRunmodePair = Pair(filename, runmode)
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.csvProcessCosmicFile() =
        produce<CosmicModel> {
            val path = Paths.get(CosmicFilenameService.resolveCosmicDataFile(filenameRunmodePair))
            CSVRecordSupplier(path).get().asSequence()
                .map { parseCosmicModel(it) }
                .forEach {
                    if (it.isValid()) {
                        (send(it))
                    }
                    delay(20L)
                }
        }
    /*

     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.apocProcessCosmicFile() =
        produce<CosmicModel> {
            val path = Paths.get(CosmicFilenameService.resolveCosmicDataFile(filenameRunmodePair))
            ApocFileReader.processDelimitedFile(path.toString())
                .map { record -> record.get("map") }
                .map { parseCosmicModel(it) }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }

    /*
    Private function to parse the Value object based on the Cosmic filename
     */
    private fun parseCosmicModel(value: Value) : CosmicModel {
        return when (filename) {
            "Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv" -> CosmicHallmark.parseValueMap(value)
            //TODO: fix this after testing
            else -> CosmicGeneCensus.parseValueMap(value)
        }
    }

    private fun parseCosmicModel(record: CSVRecord): CosmicModel {
        return when (filename) {
            "CosmicHGNC.tsv" -> CosmicHGNC.parseCSVRecord(record)
            "CosmicCompleteCNA.tsv" -> CosmicCompleteCNA.parseCSVRecord(record)
            "CosmicCompleteDifferentialMethylation.tsv" -> CosmicDiffMethylation.parseCSVRecord(record)
            "CosmicCompleteGeneExpression.tsv" -> CosmicCompleteGeneExpression.parseCSVRecord(record)
            "cancer_gene_census.csv" -> CosmicGeneCensus.parseCSVRecord(record)
            "CosmicFusionExport.tsv" -> CosmicFusion.parseCSVRecord(record)
            "classification.csv" -> CosmicClassification.parseCSVRecord(record)
            "CosmicSample.tsv" -> CosmicSample.parseCSVRecord(record)
            "CosmicMutantExportCensus.tsv" -> CosmicCodingMutation.parseCSVRecord(record)
            "Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv" -> CosmicHallmark.parseCSVRecord(record)
            "CosmicBreakpointsExport.tsv" -> CosmicBreakpoint.parseCSVRecord(record)
            "CosmicResistanceMutations.tsv" -> CosmicResistanceMutation.parseCSVRecord(record)
            "CosmicStructExport.tsv" -> CosmicStruct.parseCSVRecord(record)
            "CosmicNCV.tsv" -> CosmicNCV.parseCSVRecord(record)
            //TODO: fix this after testing
            else -> CosmicGeneCensus.parseCSVRecord(record)
        }
    }

    /*
    Private method to determine which type of file parser (APOC or Apache CSV) to use for
    a COSMIC file
     */
    private fun isCSVParserType(): Boolean {
        println("filename = $filename")
        return when (filename) {
            "Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv" -> false
            else -> true
        }
    }

    /*
    Private function to load the CosmicModel into the Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicModel(models: ReceiveChannel<CosmicModel>) =
        produce<CosmicModel> {
            for (model in models){
                Neo4jConnectionService.executeCypherCommand(model.generateLoadCosmicModelCypher())
                send(model)
                delay(20L)
            }
        }

    /*
    Private function to add Publication relationships to model objects
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processPubMedData(models: ReceiveChannel<CosmicModel>) =
        produce<CosmicModel>{
            for (model in models) {
                model.createPubMedRelationship()
                send(model)
                delay(20L)
            }
        }

    /*
    Function to load data from a Cosmic file into the Neo4j database
     */

    fun loadCosmicFile() = runBlocking {
        logger.atInfo().log("Loading Cosmic data from file: $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val models = when (isCSVParserType()){
            true ->  processPubMedData(loadCosmicModel(csvProcessCosmicFile()))
            false -> processPubMedData(loadCosmicModel(apocProcessCosmicFile()))
        }
        for ( model in models){
            nodeCount += 1
            println("Loaded ${model.getNodeIdentifier().primaryLabel}  id: ${model.getNodeIdentifier().idValue}")
        }
        logger.atInfo().log(
            " data from file: $filename loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds")
    }
}