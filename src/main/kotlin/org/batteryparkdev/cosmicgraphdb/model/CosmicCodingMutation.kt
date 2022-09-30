package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicClassificationDao
import org.batteryparkdev.cosmicgraphdb.dao.CosmicCodingMutationDao
import org.batteryparkdev.genomicgraphcore.common.*
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.neo4j.driver.Value

/*
Represents the data in the CosmicMutantExport or CosmicMutantExportCensus files
Key: mutationId
 */
data class CosmicCodingMutation(
    val geneSymbol: String, val sampleId: Int,
    val genomicMutationId: String, val geneCDSLength: Int,
    val hgncId: Int, val legacyMutationId: String,
    val mutationId: Int, val mutationCds: String,
    val mutationAA: String, val mutationDescription: String, val mutationZygosity: String,
    val LOH: String, val GRCh: String, val mutationGenomePosition: String,
    val mutationStrand: String, val resistanceMutation: String,
    val mutationSomaticStatus: String,
    val pubmedId: Int, val genomeWideScreen: Boolean,
    val hgvsp: String, val hgvsc: String, val hgvsg: String, val tier: String
) : CoreModel {

    override val idPropertyValue: String
        get() = this.mutationId.toString()

    override fun createModelRelationships() = CosmicClassificationDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String  = CosmicCodingMutationDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = geneSymbol

    override fun getModelSampleId(): String  = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(CosmicCodingMutation, this)


    override fun getPubMedIds(): List<Int>  = listOf(pubmedId)

    override fun isValid(): Boolean = geneSymbol.isNotEmpty()

    companion object: CoreModelCreator {
        override val nodename = "coding_mutation"

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
                = ::parseCsvRecord
        override val nodeIdProperty: String
            get() ="mutation_id"
        override val nodelabel: String
            get() = "CosmicCodingMutation"

          /*
          Method to parse a CsvRecord into a CosmicCodingMutation
          CosmicMutantExportCensus file is too large to use an APOC method
           */
          fun parseCsvRecord(record:CSVRecord): CosmicCodingMutation =
              CosmicCodingMutation(
                  record.get("Gene name"), // actually HGNC approved symbol
                  record.get("ID_sample").toInt(),
                  record.get("GENOMIC_MUTATION_ID"),
                  record.get("Gene CDS length").parseValidInteger(),
                  record.get("HGNC ID").parseValidInteger(),
                  record.get("LEGACY_MUTATION_ID"),
                  record.get("MUTATION_ID").toInt(),
                  record.get("Mutation CDS"),
                  record.get("Mutation AA"),
                  record.get("Mutation Description"),
                  record.get("Mutation zygosity") ?: "",
                  record.get("LOH") ?: "",
                  record.get("GRCh") ?: "38",
                  record.get("Mutation genome position"),
                  record.get("Mutation strand"),
                  record.get("Resistance Mutation"),
                  record.get("Mutation somatic status"),
                  record.get("Pubmed_PMID").parseValidInteger(),
                  record.get("Genome-wide screen").YNtoBoolean(),
                  record.get("HGVSP"),
                  record.get("HGVSC"),
                  record.get("HGVSG"),
                  record.get("Tier") ?: ""
              )
        /*
               Not all mutation files have a Tier column
          */
        private fun resolveTier(value: Value): String =
            when (value.keys().contains("Tier")) {
                true -> value["Tier"].asString()
                false -> ""
            }


    }
}