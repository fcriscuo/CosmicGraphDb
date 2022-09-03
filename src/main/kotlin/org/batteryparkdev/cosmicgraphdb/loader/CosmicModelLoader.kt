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
import org.batteryparkdev.cosmicgraphdb.model.*
import org.batteryparkdev.cosmicgraphdb.service.CosmicFilenameService
import org.batteryparkdev.io.CSVRecordSupplier
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import java.nio.file.Paths
import kotlin.streams.asSequence

class CosmicModelLoader( val filename: String) {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.csvProcessCosmicFile(dropCount: Int) =
        produce<CosmicModel> {
            val path = Paths.get(CosmicFilenameService.resolveCosmicDataFile(filename))
            CSVRecordSupplier(path).get().asSequence()
                .drop(dropCount)
                .map { parseCosmicModel(it) }
                .forEach {
                    if (it.isValid()) {
                        (send(it))
                    }
                    delay(20L)
                }
        }

    private fun parseCosmicModel(record: CSVRecord): CosmicModel {
        return when (filename) {
            "CosmicCompleteCNA.tsv" -> CosmicCompleteCNA.parseCSVRecord(record)
            "CosmicCompleteDifferentialMethylation.tsv" -> CosmicDiffMethylation.parseCSVRecord(record)
            "CosmicCompleteGeneExpression.tsv" -> CosmicCompleteGeneExpression.parseCSVRecord(record)
            "cancer_gene_census.csv" -> CosmicGeneCensus.parseCSVRecord(record)
            "CosmicFusionExport.tsv" -> CosmicFusion.parseCSVRecord(record)
            "classification.csv" -> CosmicClassification.parseCSVRecord(record)
            "CosmicSample.tsv" -> CosmicSample.parseCSVRecord(record)
            "CosmicMutantExportCensus.tsv" -> CosmicCodingMutation.parseCSVRecord(record)
            "CosmicBreakpointsExport.tsv" -> CosmicBreakpoint.parseCSVRecord(record)
            "CosmicResistanceMutations.tsv" -> CosmicResistanceMutation.parseCSVRecord(record)
            "CosmicStructExport.tsv" -> CosmicStruct.parseCSVRecord(record)
            "CosmicNCV.tsv" -> CosmicNCV.parseCSVRecord(record)
            //TODO: fix this after testing
            else -> CosmicGeneCensus.parseCSVRecord(record)
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
    fun loadCosmicFile( dropCount:Int = 0) = runBlocking {
        logger.atInfo().log("Loading Cosmic data from file: $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val models = processPubMedData(loadCosmicModel(csvProcessCosmicFile(dropCount)))

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