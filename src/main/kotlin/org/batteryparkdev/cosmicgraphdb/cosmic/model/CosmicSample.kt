package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord

data class CosmicSample(
    val sampleId: Int,
    val sampleName: String,
    val sampleType: String
) {
    companion object : AbstractModel {

        fun parseCsvRecord(record: CSVRecord): CosmicSample =
            CosmicSample(
                when (record.isMapped("ID_sample")) {
                    true -> record.get("ID_sample").toInt()
                    false -> record.get("Sample ID").toInt()

                },
                when (record.isMapped("Sample name")) {
                    true -> record.get("Sample name")
                    false -> record.get("Sample Name")
                },
                when (record.isMapped("Sample Type")) {
                    true -> record.get("Sample Type")
                    false -> ""
                }
            )
    }

}