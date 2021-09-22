package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
/*
PRIMARY_SITE	SITE_SUBTYPE_1	SITE_SUBTYPE_2	SITE_SUBTYPE_3
 */
data class CosmicType (val label: String,
                       val primary: String,
                       val subtype1: String = "NS",
                       val subtype2: String = "NS",
                       val subtype3: String = "NS") {

    companion object: AbstractModel {
        fun resolveSiteType (record:CSVRecord) :CosmicType =
            when(record.isMapped("SITE_PRIMARY")) {
               true -> CosmicType(
                    "Site", record.get("SITE_PRIMARY"),
                    record.get("SITE_SUBTYPE1"), record.get("SITE_SUBTYPE2"), record.get("SITE_SUBTYPE3")
                )
                false -> CosmicType(
                    "Site", record.get("Primary site"),
                    record.get("Site subtype 1"), record.get("Site subtype 2"), record.get("Site subtype 3")
                )
            }

        fun resolveHistologyType(record: CSVRecord): CosmicType =
            when (record.isMapped("HISTOLOGY")) {
                true -> CosmicType(
                    "Histology", record.get("HISTOLOGY"),
                    record.get("HIST_SUBTYPE1"), record.get("HIST_SUBTYPE2"), record.get("HIST_SUBTYPE3")
                )
                false ->CosmicType(
                    "Histology", record.get("Primary histology"),
                    record.get("Histology subtype 1"), record.get("Histology subtype 2"), record.get("Histology subtype 3")
                )
            }
        fun resolveTissueType (record: CSVRecord): CosmicType =
            CosmicType("Tissue", record.get("Primary Tissue"),
            record.get("Tissue Subtype 1"), record.get("Tissue Subtype 2"),"")


        fun resolveCosmicSiteType (record:CSVRecord): CosmicType =
             CosmicType("CosmicSite", record.get("SITE_PRIMARY_COSMIC"),
                record.get("SITE_SUBTYPE1_COSMIC"), record.get("SITE_SUBTYPE2_COSMIC"),
                record.get("SITE_SUBTYPE3_COSMIC"))

        fun resolveCosmicHistologyType(record:CSVRecord): CosmicType =
           CosmicType("CosmicHistology",record.get("HISTOLOGY_COSMIC"),
                record.get("HIST_SUBTYPE1_COSMIC"), record.get("HIST_SUBTYPE2_COSMIC"),
                record.get("HIST_SUBTYPE3_COSMIC")
            )
    }
}