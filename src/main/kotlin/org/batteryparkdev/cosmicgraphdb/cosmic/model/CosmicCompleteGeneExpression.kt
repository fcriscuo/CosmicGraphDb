package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

// SAMPLE_ID	SAMPLE_NAME	GENE_NAME	REGULATION	Z_SCORE	ID_STUDY
data class CosmicCompleteGeneExpression(
    val sample: CosmicSample,
    val geneName: String,
    val regulation: String,
    val zScore: Float,
    val studyId: Int
    )
{
    companion object: AbstractModel {
        fun resolveSample(record: CSVRecord): CosmicSample =
            CosmicSample(record.get("SAMPLE_ID").toInt(),
            record.get("SAMPLE_NAME"),"")

        fun parseCsvRecord(record: CSVRecord): CosmicCompleteGeneExpression =
            CosmicCompleteGeneExpression( resolveSample(record),
                record.get("GENE_NAME"),
                record.get("REGULATION"), record.get("Z_SCORE").toFloat(),
                record.get("ID_STUDY").toInt()
            )

    }
}

fun main() {
    val path = Paths.get("./data/sample_CosmicCompleteGeneExpression.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicCompleteGeneExpression.parseCsvRecord(it) }
                .forEach { exp ->
                    println(
                        "Sample: ${exp.sample.sampleName}  Gene name: ${exp.geneName} " +
                                " Regulation: ${exp.regulation} " +
                                " z-Score: ${exp.zScore}  Study Id: ${exp.studyId} "
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}