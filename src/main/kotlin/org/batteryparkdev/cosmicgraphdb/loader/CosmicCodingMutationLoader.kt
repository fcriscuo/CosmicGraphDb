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
import org.batteryparkdev.cosmicgraphdb.model.CosmicCodingMutation
import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier

object CosmicCodingMutationLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    /*
    Private function to parse the CodingMutations associated with CosmicCensus genes
    CosmicMutantExportCensus  file
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicMutantExportCensusFile(comicMutantExportCensusFile: String) =
        produce<CosmicCodingMutation> {
            ApocFileReader.processDelimitedFile(comicMutantExportCensusFile)
                .map { record -> record.get("map") }
                .map { CosmicCodingMutation.parseValueMap(it) }
                .filter {
                    Neo4jUtils.nodeExistsPredicate(
                        NodeIdentifier(
                            "CosmicGene", "gene_symbol",
                            it.geneSymbol))
                }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }

    /*
    Private function to parse the CodingMutations associated with all genes
    in the Cosmic database
    CosmicMutantExport  file
     */

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicMutantExportFile(comicMutantExportFile: String) =
        produce<CosmicCodingMutation> {
            ApocFileReader.processDelimitedFile(comicMutantExportFile)
                .map { record -> record.get("map") }
                .map { CosmicCodingMutation.parseValueMap(it) }
                .forEach {
                    send(it)
                    delay(20L)
                }
        }
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicCodingMutations(mutations: ReceiveChannel<CosmicCodingMutation>) =
        produce<CosmicCodingMutation> {
            for (mutation in mutations) {
                Neo4jConnectionService.executeCypherCommand(mutation.generateCosmicCodingMutationCypher())
                mutation.createPubMedRelationship(mutation.pubmedId)
                send(mutation)
                delay(20L)
            }
        }

    /*
    Public function to load Cosmic Coding Mutations from a specified file
     */
    fun loadCosmicCodingMutationData(filename: String) = runBlocking {
        logger.atInfo().log("Loading CosmicCodingMutation data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val mutations = when (filename.contains("Census")) {
            true -> loadCosmicCodingMutations( parseCosmicMutantExportCensusFile(filename))
            false-> loadCosmicCodingMutations(parseCosmicMutantExportFile(filename))
        }
        // consume data to initiate the pipeline
        for (mutation in mutations) {
            nodeCount += 1
        }
        logger.atInfo().log(
            "$filename data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )

    }
}


