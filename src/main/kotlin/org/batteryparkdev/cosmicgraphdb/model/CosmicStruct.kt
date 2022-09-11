package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicStructDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

/*
Represents the data in the CosmicStructExport file
Key: mutationId
Node Relationships: SampleMutationCollection -[HAS_STRUCT] -> Struct
                    Struct - [HAS_PUBLICATION] -> Publication
 */

data class CosmicStruct(
    val mutationId: Int,
    val sampleId: Int,
    val tumorId: Int,
    val mutationType: String,
    val description: String,
    val pubmedId: Int,
    val structType: String
) : CoreModel {
    override fun createModelRelationships() = CosmicStructDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String = CosmicStructDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = ""

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicStruct", "mutation_id", mutationId.toString(),
            resolveStructType(description)
        )

    override fun getPubMedIds(): List<Int> = listOf(pubmedId)

    override fun isValid(): Boolean = sampleId > 0


    companion object : CoreModelCreator {
        const val nodename = "struct"

        fun parseCsvRecord(record: CSVRecord): CosmicStruct =
            CosmicStruct(
                record.get("MUTATION_ID").parseValidInteger(),
                record.get("ID_SAMPLE").parseValidInteger(),
                record.get("ID_TUMOUR").parseValidInteger(),
                record.get("Mutation Type"),
                record.get("description"),
                record.get("PUBMED_PMID").parseValidInteger(),
                resolveStructType(record.get("description"))
            )
        private fun resolveStructType(description: String): String =
            with(description) {
                when {
                    endsWith("bkpt") -> "Breakpoint"
                    endsWith("del") -> "Deletion"
                    endsWith("ins") -> "Insertion"
                    else -> "Unspecified"
                }
            }

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
           = ::parseCsvRecord
    }
}