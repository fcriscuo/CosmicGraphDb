package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicCompleteGeneExpressionDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

// n.b The GENE_NAME column really contains the gene symbol
data class CosmicCompleteGeneExpression(
    val sampleId: Int,
    val sampleName: String,
    val geneSymbol: String,
    val regulation: String,
    val zScore: Float,
    val studyId: Int,
    val key:String
    ): CoreModel
{
    override val idPropertyValue: String
        get() = key

    override fun createModelRelationships() = CosmicCompleteGeneExpressionDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String =
        CosmicCompleteGeneExpressionDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = geneSymbol

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier =generateNodeIdentifierByModel(CosmicCompleteGeneExpression,
        this)

    override fun getPubMedIds(): List<Int> = emptyList()

    override fun isValid(): Boolean = geneSymbol.isNotEmpty()
        .and(sampleId > 0).and(sampleName.isNotEmpty())

    companion object: CoreModelCreator {
        override val nodename = "expression"

        fun parseCsvRecord(record: CSVRecord): CosmicCompleteGeneExpression =
            CosmicCompleteGeneExpression(
                record.get("SAMPLE_ID").toInt(),
                record.get("SAMPLE_NAME"),
                record.get("GENE_NAME"),
                record.get("REGULATION"),
                record.get("Z_SCORE").toFloat(),
                record.get("ID_STUDY").toInt(),
                record.get("GENE_NAME")
                    .plus(":")
                    .plus(record.get("SAMPLE_ID"))
                    .plus(":")
                    .plus(record.get("SAMPLE_NAME"))
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            = ::parseCsvRecord
        override val nodeIdProperty: String
            get() = "key"
        override val nodelabel: String
            get() = "CompleteGeneExpression"
    }
}
