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
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.io.CSVRecordSupplier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jConnectionService
import java.nio.file.Paths
import kotlin.streams.asSequence

class CoreModelLoader( val filename: String) {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.csvProcessCosmicFile(dropCount: Int) =
        produce<CoreModel> {
            val path = Paths.get(CosmicFilenameService.resolveCosmicDataFile(filename))
            CSVRecordSupplier(path).get().asSequence()
                .drop(dropCount)
                .map { it -> parseCoreModel(it)  }
                .forEach {
                    if (it.isValid()) {
                        (send(it))
                    }
                    delay(20L)
                }
        }

    // invoke the appropriate parsing function based on the COSMIC file type
    private fun parseCoreModel(record: CSVRecord): CoreModel {
        return when (filename) {
            "CosmicCompleteCNA.tsv" -> CosmicCompleteCNA.createCoreModelFunction.invoke(record)
            "CosmicCompleteDifferentialMethylation.tsv" -> CosmicDiffMethylation.createCoreModelFunction.invoke(record)
            "CosmicCompleteGeneExpression.tsv" -> CosmicCompleteGeneExpression.createCoreModelFunction.invoke(record)
            "cancer_gene_census.csv" -> CosmicGeneCensus.createCoreModelFunction.invoke(record)
            "CosmicFusionExport.tsv" -> CosmicFusion.createCoreModelFunction.invoke(record)
            "classification.csv" -> CosmicClassification.createCoreModelFunction.invoke(record)
            "CosmicSample.tsv" -> CosmicSample.createCoreModelFunction.invoke(record)
            "CosmicMutantExportCensus.tsv" -> CosmicCodingMutation.createCoreModelFunction.invoke(record)
            "CosmicBreakpointsExport.tsv" -> CosmicBreakpoint.createCoreModelFunction.invoke(record)
            "CosmicResistanceMutations.tsv" -> CosmicResistanceMutation.createCoreModelFunction.invoke(record)
            "CosmicStructExport.tsv" -> CosmicStruct.createCoreModelFunction.invoke(record)
            "CosmicNCV.tsv" -> CosmicNCV.createCoreModelFunction.invoke(record)
            //TODO: fix this after testing
            else -> CosmicGeneCensus.createCoreModelFunction.invoke(record)
        }
    }

    /*
    Private function to load the CoreModel into the Neo4j database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCoreModel(models: ReceiveChannel<CoreModel>) =
        produce<CoreModel> {
            for (model in models){
                Neo4jConnectionService.executeCypherCommand(model.generateLoadModelCypher())
                send(model)
                delay(20L)
            }
        }

    /*
    Private function to complete Neo4j relationships for these new nodes
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.processPubMedData(models: ReceiveChannel<CoreModel>) =
        produce<CoreModel>{
            for (model in models) {
                model.createModelRelationships()
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
        val models = processPubMedData(loadCoreModel(csvProcessCosmicFile(dropCount)))

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