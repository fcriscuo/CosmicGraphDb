package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicFusionDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

/*
Responsible for mapping data from the CosmicFusionExport.tsv file
Only entries that have translocation name value will be processed
Duplicate fusion ids differ only by fusion type and will be
persisted in Neo4j as a single CosmicFusion node
 */
data class CosmicFusion(
    val fusionId: Int, val sampleId: Int, val sampleName: String,
    val translocationName: String,
    val five_chromosome: Int, val five_strand: String,
    val five_geneId: Int, val five_geneSymbol: String, val five_lastObservedExon: Int,
    val five_genomeStartFrom: Int, val five_genomeStartTo: Int,
    val five_genomeStopFrom: Int, val five_genomeStopTo: Int,
    val three_chromosome: Int, val three_strand: String,
    val three_geneId: Int, val three_geneSymbol: String, val three_firstObservedExon: Int,
    val three_genomeStartFrom: Int, val three_genomeStartTo: Int,
    val three_genomeStopFrom: Int, val three_genomeStopTo: Int,
    val fusionType: String, val pubmedId: Int
) : CoreModel {
    override val idPropertyValue: String
        get() = fusionId.toString()

    override fun createModelRelationships() = CosmicFusionDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String = CosmicFusionDao(this).generateLoadCosmicModelCypher()

    //Fusion mutations involve two genes
    override fun getModelGeneSymbol(): String = five_geneSymbol.plus("|")
        .plus(three_geneSymbol)

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(CosmicFusion, this)

    override fun getPubMedIds(): List<Int> = listOf(pubmedId)

    //FusionExport file contains a number of incomplete entries
    override fun isValid(): Boolean =
        fusionType.isNotEmpty().and(fusionId > 0).
        and( sampleId > 0).and( translocationName.isNotEmpty())
            .and(five_geneSymbol.isNotEmpty()).and(three_geneSymbol.isNotEmpty())

    companion object : CoreModelCreator {
        override val nodename = "fusion"

        fun parseCsvRecord(record: CSVRecord): CosmicFusion =
            CosmicFusion(
                record.get("FUSION_ID").parseValidInteger(),
                record.get("SAMPLE_ID").parseValidInteger(),
                record.get("SAMPLE_NAME"),
                record.get("TRANSLOCATION_NAME"),
                record.get("5'_CHROMOSOME").parseValidInteger(),
                record.get("5'_STRAND"),
                record.get("5'_GENE_ID").parseValidInteger(),
                record.get("5'_GENE_NAME"),
                record.get("5'_LAST_OBSERVED_EXON").parseValidInteger(),
                record.get("5'_GENOME_START_FROM").parseValidInteger(),
                record.get("5'_GENOME_START_TO").parseValidInteger(),
                record.get("5'_GENOME_STOP_FROM").parseValidInteger(),
                record.get("5'_GENOME_STOP_TO").parseValidInteger(),
                record.get("3'_CHROMOSOME").parseValidInteger(),
                record.get("3'_STRAND"),
                record.get("3'_GENE_ID").parseValidInteger(),
                record.get("3'_GENE_NAME"),
                record.get("3'_FIRST_OBSERVED_EXON").parseValidInteger(),
                record.get("3'_GENOME_START_FROM").parseValidInteger(),
                record.get("3'_GENOME_START_TO").parseValidInteger(),
                record.get("3'_GENOME_STOP_FROM").parseValidInteger(),
                record.get("3'_GENOME_STOP_TO").parseValidInteger(),
                record.get("FUSION_TYPE").filter { it.isWhitespace().not() },
                record.get("PUBMED_PMID").toInt()
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            = ::parseCsvRecord
        override val nodeIdProperty: String
            get() = "fusion_id"
        override val nodelabel: String
            get() = "CosmicFusion"
    }
}

