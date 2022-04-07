package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.TsvRecordSequenceSupplier
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
        const val nodename = "tumor"

        fun generatePlaceholderCypher(tumorId: Int)  = " CALL apoc.merge.node([\"CosmicTumor\"], " +
                " {tumor_id = $tumorId, created: datetime()} " +
                " YIELD node as ${CosmicTumor.nodename}  \n"

        fun generateChildRelationshipCypher (tumorId: Int, childLabel: String ) :String{
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_tumor"
            return  generatePlaceholderCypher(tumorId).plus(
            " CALL apoc.merge.relationship (${CosmicTumor.nodename}, '$relationship', " +
                    " {}, {created: datetime()}, " +
                    " $childLabel, {} YIELD rel AS $relname \n")
        }


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

