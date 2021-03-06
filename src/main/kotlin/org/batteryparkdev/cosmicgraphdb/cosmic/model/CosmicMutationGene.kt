package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord

/*
Gene name
Accession Number
	Gene CDS length
	HGNC ID
 */
data class CosmicMutationGene(
    val geneSymbol: String, val accessionNumber: String,
    val cdsLength: Int, val hgncId: String
) {

    companion object {
        fun parseCsvRecord(record: CSVRecord): CosmicMutationGene =
            CosmicMutationGene(
                when (record.isMapped("Gene name")) {
                    true -> record.get("Gene name")
                    false -> record.get("Gene Name")
                },
                record.get("Accession Number"),
                record.get("Gene CDS length").toInt(), record.get("HGNC ID")
            )

    }
}