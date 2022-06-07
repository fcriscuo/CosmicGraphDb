package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.io.ApocFileReader
import org.batteryparkdev.cosmicgraphdb.model.*
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.neo4j.driver.Value

class CosmicModelLoader( val filename: String, val runmode:String = "sample") {

    private val filenameRunmodePair = Pair(filename, runmode)
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicFile() =
        produce<CosmicModel> {
            val path = CosmicFilenameService.resolveCosmicDataFile(filenameRunmodePair)
            ApocFileReader.processDelimitedFile(path)
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
            "CosmicHGNC.tsv" -> CosmicHGNC.parseValueMap(value)
            "CosmicCompleteCNA.tsv" -> CosmicCompleteCNA.parseValueMap(value)
            "CosmicCompleteDifferentialMethylation.tsv" -> CosmicDiffMethylation.parseValueMap(value)
            "CosmicCompleteGeneExpression.tsv" -> CosmicCompleteGeneExpression.parseValueMap(value)
            "cancer_gene_census.csv" -> CosmicGeneCensus.parseValueMap(value)
            "CosmicFusionExport.tsv" -> CosmicFusion.parseValueMap(value)
            "classification.csv" -> CosmicClassification.parseValueMap(value)
            "CosmicSample.tsv" -> CosmicSample.parseValueMap(value)
            "CosmicMutantExportCensus.tsv" -> CosmicCodingMutation.parseValueMap(value)
            "Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv" -> CosmicHallmark.parseValueMap(value)
            "CancerBreakpointsExport.tsv" -> CosmicBreakpoint.parseValueMap(value)
            "CosmicResistanceMutation.tsv" -> CosmicResistanceMutation.parseValueMap(value)
            "CosmicStructExport.tsv" -> CosmicStruct.parseValueMap(value)
            "CosmicNCV.tsv" -> CosmicNCV.parseValueMap(value)
            //TODO: fix this after testing
            else -> CosmicGeneCensus.parseValueMap(value)
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
    Function to load data from a Cosmic file into the Neo4j database
     */

    fun loadCosmicFile() = runBlocking {
        logger.atInfo().log("Loading Cosmic data from file: $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val models = loadCosmicModel(parseCosmicFile())
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