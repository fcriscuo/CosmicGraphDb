package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.CsvRecordSequenceSupplier
import java.nio.file.Paths

data class CosmicClassification(
    val cosmicPhenotypeId: String,
    val siteType: CosmicType,
    val histologyType: CosmicType,
    val cosmicSiteType: CosmicType,
    val cosmicHistologyType: CosmicType,
    val nciCode: String,
    val efoUrl: String
): AbstractModel {

    companion object{
        fun parseCsvRecord (record: CSVRecord): CosmicClassification {
            val site = CosmicType("Site", record.get("SITE_PRIMARY"),
              record.get("SITE_SUBTYPE1"),record.get("SITE_SUBTYPE2"),record.get("SITE_SUBTYPE3")
            )
            val  histology = CosmicType("Histology", record.get("HISTOLOGY"),
                record.get("HIST_SUBTYPE1"), record.get("HIST_SUBTYPE2"), record.get("HIST_SUBTYPE3")
            )
            val cosmicSite = CosmicType("CosmicSite", record.get("SITE_PRIMARY_COSMIC"),
                record.get("SITE_SUBTYPE1_COSMIC"), record.get("SITE_SUBTYPE2_COSMIC"),
                record.get("SITE_SUBTYPE3_COSMIC")
            )
            val cosmicHistology = CosmicType("CosmicHistology",record.get("HISTOLOGY_COSMIC"),
                record.get("HIST_SUBTYPE1_COSMIC"), record.get("HIST_SUBTYPE2_COSMIC"),
                record.get("HIST_SUBTYPE3_COSMIC")
            )
            val code = record.get("NCI_CODE") ?: "NS"
            val efo = record.get("EFO") ?: "NS"
            return CosmicClassification(record.get("COSMIC_PHENOTYPE_ID"),
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
            .forEach {
                    cc -> println("Cosmic id= ${cc.cosmicPhenotypeId}  histology= ${cc.histologyType.primary}")
                recordCount += 1
            }
        }
    println("Record count = $recordCount")
}


