package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

data class CosmicTumor(
    val tumorId: String,  val cosmicSample: CosmicSample,
    val site: CosmicType, val histology: CosmicType,
    val genomeWideScreen: Boolean,
    val pubmedId: String, val studyId: String, val sampleType: String,
    val tumorOrigin: String, val age:Int,
    val cosmicMutation: CosmicMutation
) {
    companion object : AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicTumor =
            CosmicTumor(
                record.get("ID_tumour"),CosmicSample.parseCsvRecord(record),
                CosmicType.resolveSiteType(record), CosmicType.resolveHistologyType(record),
                record.get("Genome-wide screen").lowercase() == "y",
                record.get("Pubmed_PMID"), record.get("ID_STUDY"),
                record.get("Sample Type"), record.get("Tumour origin"),
                parseValidIntegerFromString(record.get("Age")),
                CosmicMutation.parseCsvRecord(record)
                )

    }
}
fun main() {
    val path = Paths.get("./data/sample_CosmicMutantExport.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicTumor.parseCsvRecord(it) }
                .forEach { tumor ->
                    println(
                        "Tumor Id= ${tumor.tumorId}  PubMed Id= ${tumor.pubmedId}" +
                                "  Tumor origin = ${tumor.tumorOrigin} " +
                                "  sample type = ${tumor.sampleType} " +
                                "  sample name = ${tumor.cosmicSample.sampleName}" +
                                "  age = ${tumor.age} " +
                                "  mutation AA = ${tumor.cosmicMutation.mutationAA} " +
                                "  description = ${tumor.cosmicMutation.mutationDescription} "

                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}