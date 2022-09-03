package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicBreakpointDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.hgnc.HgncModel
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils


/*
Represents the data in the CosmicBreakpointsExport files
Key: mutationId
Relationships:  Struct -[HAS_BREAKPOINT]->  Breakpoint
                Breakpoint - [HAS_PUBLICATION] -> Publication

 */
data class CosmicBreakpoint(
    val sampleName: String, val sampleId: Int, val tumorId: Int,
    val mutationType: CosmicType, val mutationId: Int,
    val chromosomeFrom: String,
    val locationFromMin: Int, val locationFromMax: Int,
    val strandFrom: String, val chromosomeTo: String, val locationToMin: Int, val locationToMax: Int,
    val strandTo: String, val pubmedId: Int = 0, val studyId: Int
) : CoreModel {

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier("CosmicBreakpoint", "mutation_id", mutationId.toString())

    override fun isValid(): Boolean = (sampleId > 0).and(mutationId > 0)

    override fun getPubMedIds(): List<Int> = listOf(pubmedId)

    override fun generateLoadModelCypher(): String = CosmicBreakpointDao(this).generateCosmicBreakpointCypher()

    override fun getModelGeneSymbol(): String = ""

    override fun getModelSampleId(): String = sampleId.toString()

    companion object : CoreModelCreator {
        const val nodename = "breakpoint"

        private fun parseCsvRecord(record: CSVRecord): CosmicBreakpoint {
            val sampleName = record.get("Sample name")
            val sampleId = record.get("ID_SAMPLE").toInt()
            val tumorId = record.get("ID_TUMOUR").toInt()
            val mutationId = record.get("Mutation ID").toInt()
            val chromFrom = record.get("Chrom From")
            val locationFromMin = record.get("Location From min").toInt()
            val locationFromMax = record.get("Location From max").toInt()
            val strandFrom = record.get("Strand From")
            val chromTo = record.get("Chrom To")
            val locationToMin = record.get("Location To min").toInt()
            val locationToMax = record.get("Location To max").toInt()
            val strandTo = record.get("Strand To")
            val pubmedId = record.get("Pubmed_PMID").parseValidInteger()
            val studyId = record.get("ID_STUDY").parseValidInteger()
            return CosmicBreakpoint(
                sampleName, sampleId, tumorId,
                CosmicType("Mutation", record.get("Mutation Type")),
                mutationId, chromFrom, locationFromMin, locationFromMax, strandFrom,
                chromTo, locationToMin, locationToMax, strandTo, pubmedId, studyId
            )
        }

        override val createCoreModelFunction: (CSVRecord) -> CoreModel = ::parseCsvRecord
    }
}

