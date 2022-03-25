package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

// SAMPLE_ID	SAMPLE_NAME	GENE_NAME	REGULATION	Z_SCORE	ID_STUDY
// n.b The GENE_NAME column really contains the gene symbol
data class CosmicCompleteGeneExpression(
    val sampleId:Int,
    val geneSymbol: String,
    val regulation: String,
    val zScore: Float,
    val studyId: Int,
    val key:Int
    )
{
    companion object: AbstractModel {

        fun parseCsvRecord(record: CSVRecord): CosmicCompleteGeneExpression =
            CosmicCompleteGeneExpression(
                record.get("SAMPLE_ID").toInt(),
                record.get("GENE_NAME"),  // really gene symbol
                record.get("REGULATION"), record.get("Z_SCORE").toFloat(),
                record.get("ID_STUDY").toInt(),
                (record.get("GENE_NAME")+record.get("SAMPLE_ID")).hashCode()
            )

    }
}

fun main() {
    val path = Paths.get("./data/sample_CosmicCompleteGeneExpression.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(100)
        .forEach { it ->
            it.stream()
                .limit(100L)
                .map { CosmicCompleteGeneExpression.parseCsvRecord(it) }
                .forEach { exp ->
                    println(
                        "Sample: ${exp.sampleId}  Gene name: ${exp.geneSymbol} " +
                                " Regulation: ${exp.regulation} " +
                                " z-Score: ${exp.zScore}  Study Id: ${exp.studyId} " +
                                " Key: ${exp.key}"
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}