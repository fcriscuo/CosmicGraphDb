package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicResistanceMutationCollectionDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

data class CosmicResistanceMutation(
    val mutationId: Int,
    val genomicMutationId: String,
    val legacyMutationId: String,
    val aaMutation: String,
    val cdsMutation: String,
    val somaticStatus: String,
    val zygosity: String,
    val genomeCoordinates: String,
    val tier: Int,
    val hgvsp: String,
    val hgvsc: String,
    val hgvsg: String,
    val sampleId: Int,
    val geneSymbol: String,
    val transcript: String,
    val drugName: String,
    val pubmedId: Int
) : CoreModel {
    override fun createModelRelationships() = CosmicResistanceMutationCollectionDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String = CosmicResistanceMutationCollectionDao(this)
        .generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = geneSymbol

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicResistanceMutation", "mutation_id",
            mutationId.toString()
        )

    override fun getPubMedIds(): List<Int> = listOf(pubmedId)

    override fun isValid(): Boolean = geneSymbol.isNotEmpty()
        .and(sampleId > 0).and(mutationId > 0)


    companion object : CoreModelCreator {
        const val nodename = "resistance"

        fun parseCsvRecord(record: CSVRecord): CosmicResistanceMutation =
            CosmicResistanceMutation(
                record.get("MUTATION_ID").parseValidInteger(),
                record.get("GENOMIC_MUTATION_ID"),
                record.get("LEGACY_MUTATION_ID"),
                record.get("AA Mutation"),
                record.get("CDS Mutation"),
                record.get("Somatic Status"),
                record.get("Zygosity"),
                record.get("Genome Coordinates (GRCh38)"),
                record.get("Tier").parseValidInteger(),
                record.get("HGVSP"),
                record.get("HGVSC"),
                record.get("HGVSG"),
                record.get("Sample ID").parseValidInteger(),
                record.get("Gene Name"),
                record.get("Transcript"),
                record.get("Drug Name"),
                record.get("Pubmed Id").parseValidInteger()
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            = ::parseCsvRecord
    }
}
