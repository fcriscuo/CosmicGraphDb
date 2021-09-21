package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord

/*
Gene name
Accession Number
	Gene CDS length
	HGNC ID
 */
data class CosmicGene(val geneName:String, val accessionNumber:String,
 val cdsLength:Int, val hgncId: String) {

    companion object {
        fun parseCsvRecrod(record: CSVRecord): CosmicGene =
             CosmicGene( record.get("Gene name"), record.get("Accession Number"),
            record.get("Gene CDS length").toInt(), record.get("HGNC ID"))

    }
}