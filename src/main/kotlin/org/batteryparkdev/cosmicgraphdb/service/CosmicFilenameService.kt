package org.batteryparkdev.cosmicgraphdb.service

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.property.service.ApplicationProperties
import org.batteryparkdev.property.service.Neo4jPropertiesService

object CosmicFilenameService {

    private val config = ApplicationProperties("cosmicdb.config")
    private val neo4jUri = Neo4jPropertiesService.neo4jUri
    private val localDataDir = config.getConfigPropertyAsString("localhost.cosmic.data.directory")
    private val localSampleDataDir = config.getConfigPropertyAsString("localhost.cosmic.sample.directory")
    private val remoteDataDir = config.getConfigPropertyAsString("remote.cosmic.data.directory")
    private val remoteSampleDataDir = config.getConfigPropertyAsString("remote.cosmic.sample.directory")

    val cosmicHGNCFile = "CosmicHGNC.tsv"
    val cosmicCompleteCNAFile = "CosmicCompleteCNA.tsv"
    val cosmicDiffMethylationFile = "CosmicCompleteDifferentialMethylation.tsv"
    val cosmicGeneExpressionFile = "CosmicCompleteGeneExpression.tsv"
    val cosmicGeneCensusFile = "cancer_gene_census.csv"
    val cosmicClassificationFile = "classification.csv"
    val cosmicSampleFile = "CosmicSample.tsv"
    val cosmicMutationExportCensusFile = "CosmicMutantExportCensus.tsv"
    val cosmicHallmarkFile = "Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv"
    val cosmicBreakpointsFile = "CosmicBreakpointsExport.tsv"
    val cosmicFusionFile = "CosmicFusionExport.tsv"
    val cosmicResistanceFile = "CosmicResistanceMutations.tsv"
    val cosmicStructFile = "CosmicStructExport.tsv"
    val cosmicNCVFile = "CosmicNCV.tsv"
    val cosmicTumorTypes = "CosmicTumorTypeAbbreviations.tsv"

     val nodeNameList = listOf<String>("CosmicHGNC",
        "CosmicHallmark", "CosmicTumor", "CosmicCodingMutation",
        "CosmicSample", "CosmicClassification", "CosmicGene", "CosmicType",
        "CosmicCompleteCNA", "CosmicGeneExpression", "CosmicDiffMethylation",
        "CosmicBreakpoint", "CosmicPatient", "CosmicNCV", "CosmicFusion",
        "CosmicResistanceMutation", "GeneMutationCollection", "SampleMutationCollection",
        "Publication")

    private fun resolveSampleFile(sampleFileName: String): String =
        resolveCosmicSampleFileLocation(sampleFileName)

    private fun resolveCompleteFile(completeFileName: String): String =
        resolveCosmicCompleteFileLocation(completeFileName)

    /*
    Determine whether sample or complete COSMIC files should be loaded
     */
    fun resolveCosmicDataFile(filename: String): String {
        return when (Neo4jConnectionService.isSampleContext()) {
            false-> resolveCompleteFile(filename)
            true -> resolveSampleFile(filename)
        }
    }

    fun resolveCosmicCompleteFileLocation(filename: String): String =
        when (neo4jUri.contains("localhost")) {
            true -> localDataDir.plus("/")
                .plus(filename)
            false -> remoteDataDir.plus("/")
                .plus(filename)
        }
    fun resolveCosmicSampleFileLocation(filename: String): String =
        when (neo4jUri.contains("localhost")) {
            true -> localSampleDataDir.plus("/")
                .plus(filename)
            false -> remoteSampleDataDir.plus("/")
                .plus(filename)
        }

    private fun resolveSampleFileProperty(fileProperty: String): String =
        resolveCosmicSampleFileLocation(fileProperty)

    private fun resolveCompleteFileProperty(fileProperty: String): String =
       resolveCosmicCompleteFileLocation(fileProperty)

    fun resolveCosmicDataFileProperty(runMode: String, fileProperty: String): String =
        when (runMode.lowercase().equals("complete")) {
            true -> resolveCompleteFileProperty(fileProperty)
            false -> resolveSampleFileProperty(fileProperty)
        }
}