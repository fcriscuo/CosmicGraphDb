package org.batteryparkdev.cosmicgraphdb.service

import org.batteryparkdev.neo4j.service.Neo4jConnectionService
import org.batteryparkdev.property.service.ConfigurationPropertiesService

object CosmicFilenameService {
    /*
    Cosmic file names - current as of COSMIC Rel96 (June 2022)
     */
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

     val nodeNameList = listOf<String>("CosmicHGNC",
        "CosmicHallmark", "CosmicTumor", "CosmicCodingMutation",
        "CosmicSample", "CosmicClassification", "CosmicGene", "CosmicType",
        "CosmicCompleteCNA", "CosmicGeneExpression", "CosmicDiffMethylation",
        "CosmicBreakpoint", "CosmicPatient", "CosmicNCV", "CosmicFusion",
        "CosmicResistanceMutation", "GeneMutationCollection", "SampleMutationCollection",
        "Publication")

    private fun resolveSampleFile(sampleFileName: String): String =
        ConfigurationPropertiesService.resolveCosmicSampleFileLocation(sampleFileName)

    private fun resolveCompleteFile(completeFileName: String): String =
        ConfigurationPropertiesService.resolveCosmicCompleteFileLocation(completeFileName)

    /*
    Determine whether sample or complete COSMIC files should be loaded
     */
    fun resolveCosmicDataFile(filename: String): String {
        return when (Neo4jConnectionService.isSampleContext()) {
            false-> resolveCompleteFile(filename)
            true -> resolveSampleFile(filename)
        }
    }
}