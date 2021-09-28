package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths


data class CosmicHallmark(
    val hallmarkId: Int,   // needed to establish unique database identifier
    val geneSymbol: String, val cellType: String, val pubmedId: String,
    val hallmark: String, val impact: String, val description: String
)
{
    companion object: AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicHallmark  =
            CosmicHallmark(
                record.hashCode(),
                record.get("GENE_NAME"),
                record.get("CELL_TYPE"),
                record.get("PUBMED_PMID"),
                removeInternalQuotes(record.get("HALLMARK")),
                record.get("IMPACT"),
                removeInternalQuotes(record.get("DESCRIPTION"))
            )
    }
}

fun main() {
    val path = Paths.get("./data/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .peek { println("Record hashcode = ${it.hashCode()}") }
                .map { CosmicHallmark.parseCsvRecord(it) }
                .forEach { hallmark ->
                    println(
                        "Gene: ${hallmark.geneSymbol}  Hallmark: ${hallmark.hallmark} " +
                                " PubMed ID: ${hallmark.pubmedId} " +
                                " Cell Type: ${hallmark.cellType}  Impact: ${hallmark.impact}" +
                                " Description: ${hallmark.description} \n" +
                                "hashcode = ${hallmark.hashCode()}"
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}