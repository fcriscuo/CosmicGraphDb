package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

/*
Gene name
Accession Number
	Gene CDS length
	HGNC ID
	Sample name
	ID_sample
	ID_tumour
	Primary site	Site subtype 1	Site subtype 2	Site subtype 3
	Primary histology	Histology subtype 1	Histology subtype 2	Histology subtype 3
	Genome-wide screen
	GENOMIC_MUTATION_ID	LEGACY_MUTATION_ID	MUTATION_ID	Mutation CDS
	Mutation AA	Mutation Description	Mutation zygosity
	LOH	GRCh
	Mutation genome position
	Mutation strand
	SNP
	Resistance Mutation
	FATHMM prediction
	FATHMM score
	Mutation somatic status
	Pubmed_PMID	ID_STUDY
	Sample Type
	Tumour origin
	Age
	HGVSP
	HGVSC
	HGVSG
 */
data class CosmicMutantExport(
    val gene: CosmicGene,
    val sample: CosmicSample, val tumor: CosmicTumor,
    val genomicMutation: CosmicMutation
) {

    companion object: AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicMutantExport =
            CosmicMutantExport(
                CosmicGene.parseCsvRecord(record),
                CosmicSample.parseCsvRecord(record),
                CosmicTumor.parseCsvRecord(record),
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
                .map { CosmicMutantExport.parseCsvRecord(it) }
                .forEach { export ->
                    println(
                        "Tumor Id: ${export.tumor.tumorId}  Gene name: ${export.gene.geneName} " +
                                " AA Mut: ${export.genomicMutation.mutationAA}   Position: ${export.genomicMutation.mutationGenomePosition}" +
                                " Primary Site: ${export.tumor.site.primary} " +
                                " SampleName: ${export.sample.sampleName}  Histology: ${export.tumor.histology.primary}" +
                                " FATHMM Prediction: ${export.genomicMutation.fathmmPrediction} "
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}