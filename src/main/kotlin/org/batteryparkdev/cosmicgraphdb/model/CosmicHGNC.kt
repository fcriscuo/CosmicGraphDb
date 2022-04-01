package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

/*
COSMIC_ID	COSMIC_GENE_NAME	Entrez_id	HGNC_ID	Mutated?	Cancer_census?	Expert Curated?
 */
data class CosmicHGNC (
    val cosmicId: String,
    val cosmicGeneName: String,
    val entrezId: String,
    val hgncId: String,
    val isMutated: Boolean,
    val isCancerCensus: Boolean,
    val isExpertCurrated:Boolean
        )
{
    companion object: AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicHGNC =
            CosmicHGNC(
                record.get("COSMIC_ID"),
                record.get("COSMIC_GENE_NAME"),
                record.get("Entrez_id"),
                record.get("HGNC_ID"),
                convertYNtoBoolean(record.get("Mutated?")),
                convertYNtoBoolean(record.get("Cancer_census?")),
                convertYNtoBoolean(record.get("Expert Curated?"))
            )
    }
}
fun main() {
    val path = Paths.get("./data/CosmicHGNC.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicHGNC.parseCsvRecord(it) }
                .forEach { hgnc ->
                    println(
                        "Cosmic Id: ${hgnc.cosmicId}  Gene name: ${hgnc.cosmicGeneName} " +
                                " Entrez Id: ${hgnc.entrezId} " +
                                " HGNC Id: ${hgnc.hgncId}  Is mutated?: ${hgnc.isMutated}" +
                                " Is Census: ${hgnc.isCancerCensus}  Is Currated?: ${hgnc.isExpertCurrated}"
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}