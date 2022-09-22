package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicGeneCensusDao
import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.parseOnComma
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

data class CosmicGeneCensus(
    val geneSymbol: String, val geneName: String, val entrezGeneId: Int,
    val genomeLocation: String, val tier: Int = 0, val hallmark: Boolean = false,
    val chromosomeBand: String, val somatic: Boolean = false, val germline: Boolean,
    val somaticTumorTypeList: List<String>, val germlineTumorTypeList: List<String>,
    val cancerSyndrome: String, val tissueTypeList: List<String>, val molecularGenetics: List<String>,
    val roleInCancerList: List<String>, val mutationTypeList: List<String>,
    val translocationPartnerList: List<String>,
    val otherGermlineMut: String, val otherSyndromeList: List<String>,
    val cosmicId: String, val cosmicGeneName: String,
    val synonymList: List<String>
) : CoreModel {
    override val idPropertyValue: String
        get() = geneSymbol

    override fun createModelRelationships() = CosmicGeneCensusDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String = CosmicGeneCensusDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = geneSymbol

    override fun getModelSampleId(): String = ""

    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(CosmicGeneCensus, this)

    override fun getPubMedIds(): List<Int> = emptyList()

    override fun isValid(): Boolean = geneSymbol.isNotEmpty()

    companion object : CoreModelCreator {
        override val nodename = "gene_census"
        const val mutCollNodename = "gene_mut_collection"
        const val annoCollNodename = "gene_anno_collection"

        fun parseCsvRecord(record: CSVRecord): CosmicGeneCensus =
            CosmicGeneCensus(
                record.get("Gene Symbol"),
                record.get("Name"),
                record.get("Entrez GeneId").parseValidInteger(),
                record.get("Genome Location"),
                record.get("Tier").parseValidInteger(),
                record.get("Hallmark").isNotBlank(),
                record.get("Chr Band"),
                record.get("Somatic").isNotBlank(),
                record.get("Germline").isNotBlank(),
                processTumorTypes(record.get("Tumour Types(Somatic)")),
                processTumorTypes(record.get("Tumour Types(Germline)")),
                record.get("Cancer Syndrome"),
                record.get("Tissue Type").parseOnComma(),
                record.get("Molecular Genetics").parseOnComma(),
                record.get("Role in Cancer").parseOnComma(),
                record.get("Mutation Types").parseOnComma(),
                record.get("Translocation Partner").parseOnComma(),
                record.get("Other Germline Mut"),
               record.get("Other Syndrome").parseOnComma(),
                record.get("COSMIC ID"),
                record.get("cosmic gene name"),
                record.get("Synonyms").parseOnComma()
            )

        /*
    Function to resolve a tumor type abbreviations
     */
        private fun processTumorTypes(tumorTypes: String): List<String> {
            val tumorTypeList = mutableListOf<String>()
           tumorTypes.parseOnComma().forEach {
                tumorTypeList.add(TumorTypeService.resolveTumorType(it))
            }
            return tumorTypeList.toList()  // make List immutable
        }

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            = ::parseCsvRecord
        override val nodeIdProperty: String
            get() = "gene_symbol"
        override val nodelabel: String
            get() = "CosmicGene"
    }
}

