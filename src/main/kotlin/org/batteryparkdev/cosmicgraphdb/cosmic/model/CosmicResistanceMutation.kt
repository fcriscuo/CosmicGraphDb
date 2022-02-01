package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

/*

Sample Name	Sample ID	Gene Name
Transcript	Census Gene	Drug Name
MUTATION_ID	GENOMIC_MUTATION_ID	LEGACY_MUTATION_ID	AA Mutation	CDS Mutation
Primary Tissue	Tissue Subtype 1	Tissue Subtype 2
Histology	Histology Subtype 1	Histology Subtype 2
Pubmed Id	CGP Study	Somatic Status	Sample Type	Zygosity
Genome Coordinates (GRCh38)	Tier	HGVSP	HGVSC	HGVSG
 */

data class CosmicResistanceMutation(
    val sampleId: Int,
    val geneName: String,
    val transcript: String,
    val censusGene: Boolean,
    val drugName: String,
    val cosmicMutation: CosmicMutation,
    val cosmicTissueType: CosmicType,
    val cosmicHistology: CosmicType,
    val pubmedId: String,
    val cgpStudy: String,
    val somaticStatus: String,
    val zygosity: String,
    val genomeCoordinates: String,
    val tier: Int
) {

    companion object : AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicResistanceMutation =
            CosmicResistanceMutation(
                record.get("Sample ID").toInt(),
                record.get("Gene Name"),
                record.get("Transcript"),
                when (record.get("Census Gene")) {
                    "Yes" -> true
                    else -> false
                },
                record.get("Drug Name"),
                CosmicMutation.parseResistanceMutationCsvRecord(record),
                CosmicType.resolveTissueType(record),
                CosmicType.resolveHistologyTypeBySource(record,"CosmicResistanceMutation"),
                record.get("Pubmed Id"),
                record.get("CGP Study"),
                record.get("Somatic Status"),
                record.get("Zygosity"),
                record.get("Genome Coordinates (GRCh38)"),
                parseValidIntegerFromString(record.get("Tier"))
            )

        /*
        The CosmicResistanceMutations file has non-standard column names
        for histology columns
         */

    }
}

fun main() {
    val path = Paths.get("./data/CosmicResistanceMutations.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicResistanceMutation.parseCsvRecord(it) }
                .forEach { mut ->
                    println(
                        "Sample Id: ${mut.sampleId}  Transcript: ${mut.transcript} " +
                                " Drug name: ${mut.drugName}  Mutation: ${mut.cosmicMutation.mutationAA} " +
                                "Histology: ${mut.cosmicHistology.primary}"
                    )
                    recordCount += 1
                }

        }
    println("Record count $recordCount")
}
