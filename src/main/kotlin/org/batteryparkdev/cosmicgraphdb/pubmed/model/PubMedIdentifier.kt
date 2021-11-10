package org.batteryparkdev.cosmicgraphdb.pubmed.model

import org.apache.commons.csv.CSVRecord

data class PubMedIdentifier (val pubmedId: Int, val parentId: Int = 0, val label: String = "")
{
    companion object {
        val referenceLabel = "Reference"
        fun parseCsvRecord(record: CSVRecord,
                           parentId: Int = 0,
                           label: String = "") =
            PubMedIdentifier(record.get("Pubmed_PMID").toInt(),
             parentId, label)
    }

    fun generateReferenceIdentifierSet(pubmedEntry: PubMedEntry): Set<PubMedIdentifier>{
        val refSet = mutableSetOf<PubMedIdentifier>()
        pubmedEntry.referenceSet.forEach { id ->
            refSet.add(PubMedIdentifier(id, pubmedEntry.pubmedId, referenceLabel ))
        }
        return refSet.toSet()
    }
}