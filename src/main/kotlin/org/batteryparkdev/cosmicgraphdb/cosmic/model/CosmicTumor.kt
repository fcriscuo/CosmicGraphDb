package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

data class CosmicTumor(
    val tumorId: Int, val sampleId: Int, val mutationId: Int,
    val site: CosmicType, val histology: CosmicType,
    val genomeWideScreen: Boolean,
    val pubmedId: Int, val studyId: String, val sampleType: String,
    val tumorOrigin: String, val age: Int,
    val cosmicMutation: CosmicMutation
) {
    companion object : AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicTumor =
            CosmicTumor(
                record.get("ID_tumour").toInt(), record.get("ID_sample").toInt(),
                record.get("MUTATION_ID").toInt(),
                CosmicType.resolveSiteTypeBySource(record, "CosmicTumor"),
                CosmicType.resolveHistologyTypeBySource(record, "CosmicTumor"),
                record.get("Genome-wide screen").lowercase() == "y",
                parseValidIntegerFromString(record.get("Pubmed_PMID")), record.get("ID_STUDY"),
                record.get("Sample Type"), record.get("Tumour origin"),
                parseValidIntegerFromString(record.get("Age")),
                CosmicMutation.parseCsvRecord(record)
            )
    }
}

fun main() {
    val path = Paths.get("./data/sample_CosmicMutantExportCensus.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicTumor.parseCsvRecord(it) }
                .forEach { tumor ->
                    println(
                        "Tumor Id= ${tumor.tumorId}  Mutation id= ${tumor.mutationId}" +
                                "  Tumor origin = ${tumor.tumorOrigin} " +
                                "  gene = ${tumor.cosmicMutation.geneSymbol} " +
                                "   pubmed id = ${tumor.pubmedId}" +
                                "  sample id = ${tumor.sampleId}" +
                                "  mutation id = ${tumor.cosmicMutation.mutationId} " +
                                "  mutation AA = ${tumor.cosmicMutation.mutationAA} " +
                                "  tumor pubmed id = ${tumor.pubmedId} "
                    )
                    recordCount += 1
                }


        }
    println("Record count = $recordCount")
}

