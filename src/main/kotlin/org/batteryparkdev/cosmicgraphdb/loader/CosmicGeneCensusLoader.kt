package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.addGeneCensusLabel
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.loadCosmicGeneNode
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.loadMutationTypeAnnotations
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.loadOtherSyndromeAnnotations
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.loadRoleInCancerAnnotations
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.loadSynonymAnnotations
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.loadTissueTypeAnnotations
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.loadTranslocPartnerList
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneDao.loadTumorList
import org.batteryparkdev.io.CsvRecordSequenceSupplier
import java.nio.file.Paths

/*
Responsible for creating/merging  CosmicGeneCensus nodes and associated annotation nodes
 */
object CosmicGeneCensusLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.parseCosmicGeneCensusFile(cosmicGeneCensusFile: String) =
        produce<CosmicGeneCensus> {
            val path = Paths.get(cosmicGeneCensusFile)
            CsvRecordSequenceSupplier(path).get()
                .forEach {
                    send (CosmicGeneCensus.parseCsvRecord(it))
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadCosmicGeneCensusData( genes: ReceiveChannel<CosmicGeneCensus>) =
    produce<CosmicGeneCensus> {
        for (gene in genes) {
            loadCosmicGeneNode(gene)
            send(gene)
            delay(20)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadSomaticTumors(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus>{
            for (gene in genes) {
                loadTumorList(gene.geneSymbol, gene.somaticTumorTypeList, "Somatic")
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadGermlineTumors(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus>{
            for (gene in genes) {
                loadTumorList(gene.geneSymbol, gene.germlineTumorTypeList, "GermLine")
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadSynonymAnnotations(genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes){
                loadSynonymAnnotations(gene.geneSymbol, gene.synonymList)
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadRoleInCancerAnnotations( genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes) {
                loadRoleInCancerAnnotations(gene.geneSymbol, gene.roleInCancerList)
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadMutationTypeAnnotations( genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes) {
                loadMutationTypeAnnotations(gene.geneSymbol, gene.mutationTypeList)
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadTissueTypeAnnotations( genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes) {
                loadTissueTypeAnnotations(gene.geneSymbol, gene.tissueTypeList)
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadOtherSyndromeAnnotations( genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<CosmicGeneCensus> {
            for (gene in genes) {
                loadOtherSyndromeAnnotations(gene.geneSymbol, gene.otherSyndromeList)
                send(gene)
                delay(20)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.loadTranslocPartnerList( genes: ReceiveChannel<CosmicGeneCensus>) =
        produce<String> {
            for (gene in genes) {
                loadTranslocPartnerList(gene.geneSymbol,gene.translocationPartnerList)
                send(gene.geneSymbol)
                delay(20)
            }
        }
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.addCensusLabel( geneSymbols: ReceiveChannel<String>) =
        produce<String> {
            for (geneSymbol in geneSymbols) {
                addGeneCensusLabel(geneSymbol)
                send(geneSymbol)
                delay(10)
            }
        }
/*
Public function load CosmicGeneCensus nodes and associated annotations
 */
    fun loadCosmicGeneCensusData(filename: String) =  runBlocking {
        logger.atInfo().log("Loading CosmicGeneCensus data from file $filename")
        var nodeCount = 0
        val stopwatch = Stopwatch.createStarted()
        val geneSymbols = addCensusLabel(
            loadTranslocPartnerList(
            loadOtherSyndromeAnnotations(
                loadTissueTypeAnnotations(
                    loadMutationTypeAnnotations(
                        loadRoleInCancerAnnotations(
                            loadSynonymAnnotations(
                                loadGermlineTumors(
                                    loadSomaticTumors(
                                       loadCosmicGeneCensusData(
                                           parseCosmicGeneCensusFile(filename)
                                       ))))))))))
        for (symbol in geneSymbols) {
            // pipeline stream is lazy - need to consume output
            nodeCount += 1
        }
        logger.atInfo().log(
            "CosmicGeneCensus data loaded " +
                    " $nodeCount nodes in " +
                    " ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"
        )

    }
}
/*
main function for integration testing
 */
fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "./data/sample_cancer_gene_census.csv"
    CosmicGeneCensusLoader.loadCosmicGeneCensusData(filename)
}