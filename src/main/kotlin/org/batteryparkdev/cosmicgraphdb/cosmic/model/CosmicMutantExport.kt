@file:JvmName("CosmicMutantExportCensusKt")

package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths


/*
This class can be used to process data from either the
CosmicMutantExport.tsv file or the CosmicMutantExportCensus.tsv file
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
    val path = Paths.get("./data/sample_CosmicMutantExportCensus.tsv")
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