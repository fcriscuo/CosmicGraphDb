package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicHallmarkDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.common.removeInternalQuotes
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils
import java.util.*

data class CosmicHallmark(
    val hallmarkId: Int,   // needed to establish unique database identifier
    val geneSymbol: String, val cellType: String, val pubmedId: Int,
    val hallmark: String, val impact: String, val description: String
) : CoreModel {
    override fun createModelRelationships() = CosmicHallmarkDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String = CosmicHallmarkDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = geneSymbol

    override fun getModelSampleId(): String = ""

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicHallmark", "hallmark_id",
            hallmarkId.toString()
        )

    override fun getPubMedIds(): List<Int> = listOf(pubmedId)

    override fun isValid(): Boolean = geneSymbol.isNotEmpty().and(hallmark.isNotEmpty())


    companion object : CoreModelCreator {
        const val nodename = "hallmark"
        const val collectionname = "hallmark_collect"

        fun parseCsvRecord(record: CSVRecord): CosmicHallmark =
            CosmicHallmark(
                UUID.randomUUID().hashCode(),  // unique identifier for key
                record.get("GENE_NAME"),
                record.get("CELL_TYPE"),
               record.get("PUBMED_PMID").parseValidInteger(),
                record.get("HALLMARK").removeInternalQuotes(),
                resolveImpactProperty(record),
                record.get("DESCRIPTION").removeInternalQuotes())

         private fun resolveImpactProperty(record: CSVRecord):String =
            when (record.isMapped("IMPACT")) {
                true -> record.get("IMPACT")
                false -> "NS"
            }

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
                = ::parseCsvRecord
    }
}
