package org.batteryparkdev.cosmicgraphdb.io

import java.nio.charset.Charset
import java.nio.file.Paths
import kotlin.io.path.forEachLine

class TestReadingCosmicFiles {

    val cosmicFileList = listOf<String>(
        "CosmicHGNC.tsv",
        "CosmicCompleteDifferentialMethylation.tsv", "CosmicCompleteGeneExpression.tsv",
        "cancer_gene_census.csv", "classification.csv",
        "CosmicMutantExportCensus.tsv", "Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv",
        "CosmicBreakpointsExport.tsv", "CosmicFusionExport.tsv", "CosmicResistanceMutations.tsv",
        "CosmicStructExport.tsv", "CosmicCompleteCNA.tsv", "CosmicNCV.tsv",
        "CosmicSample.tsv"
    )
    val fileDirectory = "/Volumes/SSD870/COSMIC_rel96/"
    fun processCosmicFiles() {
        cosmicFileList.forEach { file -> readFile(file) }
    }

    private fun resolveCompleteFile(completeFileName: String): String =
        fileDirectory.plus(completeFileName)

    fun readFile(filename: String) {
        val filePath = Paths.get(resolveCompleteFile(filename))
        println("Reading file $filePath")
        var rowcount = 0
        try {
            filePath.forEachLine(Charset.forName("ISO-8859-1")) {
                rowcount += 1
            }
        } catch (e: Exception) {
            println("Exception reading file $filename at row $rowcount")
            println(e.message)
        }
        println("Row count for file: $filePath = $rowcount")
    }
}

fun main() = TestReadingCosmicFiles().processCosmicFiles()
