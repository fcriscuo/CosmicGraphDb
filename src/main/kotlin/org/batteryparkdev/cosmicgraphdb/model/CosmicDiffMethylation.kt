package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

data class CosmicDiffMethylation(
    val studyId: Int, val sampleId: Int,
    val tumorId: Int, val site: CosmicType,
    val histology: CosmicType, val fragmentId: String,
    // n.b. numeric values for chromosomes (x=23, y=24)
    val genomeVersion: String, val chromosome: Int,
    val position: Int, val strand: String,
    val geneName: String, val methylation: String,
    val avgBetaValueNormal: Float, val betaValue: Float,
    val twoSidedPValue: Double
) {

    companion object : AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicDiffMethylation =
            CosmicDiffMethylation(
                record.get("STUDY_ID").toInt(),
                record.get("ID_SAMPLE").toInt(),
                record.get("ID_TUMOUR").toInt(),
                CosmicType.resolveSiteTypeBySource(record, "CosmicDiffMethylation"),
                CosmicType.resolveHistologyTypeBySource(record, "CosmicDiffMethylation"),
                record.get("FRAGMENT_ID"),
                record.get("GENOME_VERSION"),
                record.get("CHROMOSOME").toInt(),  // Integer is OK
                record.get("POSITION").toInt(),
                when (record.get("STRAND")) {
                    "1" -> "+"
                    else -> "-"
                },
                record.get("GENE_NAME"),
                record.get("METHYLATION"),
                record.get("AVG_BETA_VALUE_NORMAL").toFloat(),
                record.get("BETA_VALUE").toFloat(),
                record.get("TWO_SIDED_P_VALUE").toDouble()
            )

    }
}

fun main() {
    val path = Paths.get("./data/sample_CosmicCompleteDifferentialMethylation.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicDiffMethylation.parseCsvRecord(it) }
                .forEach { methyl ->
                    println(
                        "Tumor Id: ${methyl.tumorId}  Gene name: ${methyl.geneName} " +
                                " Chromosome: ${methyl.chromosome}   Position: ${methyl.position}" +
                                " Sample Id: ${methyl.sampleId}  Histology: ${methyl.histology.primary}" +
                                " Methylation: ${methyl.methylation} "
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}