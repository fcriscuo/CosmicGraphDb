package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.CsvRecordSequenceSupplier
import java.nio.file.Paths

 data class CosmicClassification(
    val cosmicPhenotypeId: String,
    val siteType: CosmicType,
    val histologyType: CosmicType,
    val cosmicSiteType: CosmicType,
    val cosmicHistologyType: CosmicType,
    val nciCode: String,
    val efoUrl: String
) {

    companion object : AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicClassification {
            val site = CosmicType.resolveSiteTypeBySource(record,"CosmicClassification")
            val histology = CosmicType.resolveHistologyTypeBySource(record,"CosmicClassification")
            val cosmicSite = CosmicType.resolveCosmicSiteType(record)
            val cosmicHistology = CosmicType.resolveCosmicHistologyType(record)
            val code = record.get("NCI_CODE") ?: "NS"
            val efo = record.get("EFO") ?: "NS"
            return CosmicClassification(
                record.get("COSMIC_PHENOTYPE_ID"),
                site, histology, cosmicSite, cosmicHistology, code, efo
            )
        }
    }
}

fun main() {
    val path = Paths.get("./data/classification.csv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    CsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicClassification.parseCsvRecord(it) }
                .forEach { cc ->
                    println("Cosmic id= ${cc.cosmicPhenotypeId}  histology= ${cc.histologyType.primary}" +
                            " Site = ${cc.siteType.primary}")
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}


