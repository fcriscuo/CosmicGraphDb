package org.batteryparkdev.cosmicgraphdb.pubmed.app

import com.google.common.base.Stopwatch
import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicTumor
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService
import org.batteryparkdev.cosmicgraphdb.pubmed.loader.PubMedLoader
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

/*
Application that will load data for PubMed entries cited in a specified COSMIC
mutant export file. The loaded PubMed entries include the specified PubMed articles,
the PubMed entries referenced by those articles, and the PubMed artivles that
cite the specified article.
 */

class CosmicPubMedLoaderApp  (fileDirectory: String){
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();
    // For COSMIC data, The PubMed Ids to be loaded are in a CosmicMutantExport-formatted file
    private val cosmicMutationExportCensusFile = fileDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.mutant.export.census")
    private val pubmedNodeLabels = listOf<String>("Origin","Reference", "Citation")

    fun loadCosmicPubMedData() {
        // delete existing pubmed nodes and relationships
        pubmedNodeLabels.forEach { Neo4jUtils.detachAndDeleteNodesByName(it) }
        val path = Paths.get(cosmicMutationExportCensusFile)
        TsvRecordSequenceSupplier(path).get().chunked(500)
            .forEach { it ->
                it.stream()
                    .map { CosmicTumor.parseCsvRecord(it) }
                    .filter { it.pubmedId >0 }  // ignore records w/o pubmedId value
                    .forEach { tumor ->
                        PubMedLoader.loadPubMedEntryById(tumor.pubmedId)
                       logger.atFine().log("Loaded PubMed Article Id : ${tumor.pubmedId}")
                        Thread.sleep(300L)
                    }
            }
    }
}

fun main(args: Array<String>) {
    val fileDirectory =
        when (args.isNotEmpty()) {
            true -> args[0]
            false -> DatafilePropertiesService.resolvePropertyAsString("cosmic.data.directory")
        }
    println("WARNING: Invoking this application will delete all COSMIC PubMed data from the database")
    println("There will be a 20 second grace period to cancel (CTRL-C) this execution if this is not your intent")
    println("The application implements a 300 millisecond delay between NCBI Web-based requests " +
            "to accommodate NCBI's request rate limit")
    println("As a result, loading the PubMed entries referenced in COSMIC will take a considerable amount of time")
    Thread.sleep(20_000L)
    println("Data for PubMed entries listed in the COSMIC data will now be retrieved and loaded into Neo4j")

    if (fileDirectory.isNotEmpty()) {
        val timer = Stopwatch.createStarted()
        CosmicPubMedLoaderApp(fileDirectory).loadCosmicPubMedData()
        timer.stop()
        println("++++ COSMIC PubMed data loaded  in ${timer.elapsed(TimeUnit.MINUTES)} minutes+++")
    }

}