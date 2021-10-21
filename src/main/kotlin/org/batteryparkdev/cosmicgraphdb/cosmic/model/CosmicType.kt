package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

/*
PRIMARY_SITE	SITE_SUBTYPE_1	SITE_SUBTYPE_2	SITE_SUBTYPE_3
 */
data class CosmicType(
    val label: String,
    val primary: String,
    val subtype1: String = "NS",
    val subtype2: String = "NS",
    val subtype3: String = "NS"
) {
    // generate a unique identifier for databaase
    fun generateIdentifier():Int = (primary+subtype1+subtype2+subtype3).hashCode()

    //Some properties representing the same data have,
    //similar by distinct names and positions in different Cosmic files
    // add more when clauses as more file types are supported
    companion object : AbstractModel {
        fun resolveSiteTypeBySource(record: CSVRecord, source: String): CosmicType =
            when (source) {
                "CosmicClassification" -> CosmicType(
                    "Site", record.get("SITE_PRIMARY"),
                    record.get("SITE_SUBTYPE1"), record.get("SITE_SUBTYPE2"), record.get("SITE_SUBTYPE3")
                )
                "CosmicSample" -> CosmicType(
                    "Site", record.get("primary_site"),
                    record.get("site_subtype_1"), record.get("site_subtype_2"), record.get("site_subtype_3")

                )
                "CosmicDiffMethylation" -> CosmicType(
                    "Site", record.get("PRIMARY_SITE"),
                    record.get("SITE_SUBTYPE_1"), record.get("SITE_SUBTYPE_2"), record.get("SITE_SUBTYPE_3")
                )
                // CosmicTumor, CosmicCNA
                else -> CosmicType(
                    "Site", record.get("Primary site"),
                    record.get("Site subtype 1"), record.get("Site subtype 2"), record.get("Site subtype 3")
                )
            }

        fun resolveHistologyTypeBySource(record: CSVRecord, source: String): CosmicType =
            when (source) {
                "CosmicClassification" -> CosmicType(
                    "Histology", record.get("HISTOLOGY"),
                    record.get("HIST_SUBTYPE1"), record.get("HIST_SUBTYPE2"), record.get("HIST_SUBTYPE3")
                )
                "CosmicSample" -> CosmicType(
                    "Histology", record.get("primary_histology"),
                    record.get("histology_subtype_1"), record.get("histology_subtype_2")
                )
                "CosmicDiffMethylation" -> CosmicType("Histology", record.get("PRIMARY_HISTOLOGY"),
                        record.get("HISTOLOGY_SUBTYPE_1"), record.get("HISTOLOGY_SUBTYPE_2"),
                    record.get("HISTOLOGY_SUBTYPE_3"))
                "CosmicResistanceMutation" ->  CosmicType(
                    "Histology", record.get("Histology"),
                    record.get("Histology Subtype 1"), record.get("Histology Subtype 2"),
                    "")
                // CosmicTumor, CosmicCNA
                else -> CosmicType(
                    "Histology",
                    record.get("Primary histology"),
                    record.get("Histology subtype 1"),
                    record.get("Histology subtype 2"),
                    record.get("Histology subtype 3")
                )
            }

        fun resolveTissueType(record: CSVRecord): CosmicType =
            CosmicType(
                "Tissue", record.get("Primary Tissue"),
                record.get("Tissue Subtype 1"), record.get("Tissue Subtype 2"), ""
            )

        fun resolveCosmicSiteType(record: CSVRecord): CosmicType =
            CosmicType(
                "CosmicSite", record.get("SITE_PRIMARY_COSMIC"),
                record.get("SITE_SUBTYPE1_COSMIC"), record.get("SITE_SUBTYPE2_COSMIC"),
                record.get("SITE_SUBTYPE3_COSMIC")
            )

        fun resolveCosmicHistologyType(record: CSVRecord): CosmicType =
            CosmicType(
                "CosmicHistology", record.get("HISTOLOGY_COSMIC"),
                record.get("HIST_SUBTYPE1_COSMIC"), record.get("HIST_SUBTYPE2_COSMIC"),
                record.get("HIST_SUBTYPE3_COSMIC")
            )

    }
}
fun main() {
    val path = Paths.get("./data/sample_CosmicSample.tsv")
    println("Processing Cosmic file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicSample.parseCsvRecord(it) }
                .forEach { sample ->
                    println(
                        "Sample Id= ${sample.sampleId}  SampleType= ${sample.sampleType}" +
                                "  Primary Site = ${sample.site.primary}  " +
                                " ${sample.site.subtype1} " +
                                " ${sample.site.subtype2} " +
                                " ${sample.site.subtype3}  " +
                                " site id: ${sample.site.generateIdentifier()}" +
                                " \nHistology = ${sample.histology.primary}  " +
                                "histology id = ${sample.histology.generateIdentifier()}"

                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}