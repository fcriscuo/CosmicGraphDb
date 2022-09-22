package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicNCVDao
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.YNtoBoolean
import org.batteryparkdev.genomicgraphcore.common.parseValidInteger
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

data class CosmicNCV(
  val sampleName: String, val sampleId: Int,val tumorId: Int,
  val genomicMutationId: String, val legacyMutationId: String,
  val zygosity: String, val grch: Int,
  val genomePosition: String, val mutationSomaticStatus: String,
  val wtSeq: String, val mutSeq: String,
  val wholeGenomeReseq: Boolean, val wholeExome:Boolean, val studyId: Int,
  val pubmedId: Int, val hgvsg: String
): CoreModel  {
    override val idPropertyValue: String
        get() = genomicMutationId

    override fun createModelRelationships() = CosmicNCVDao.modelRelationshipFunctions.invoke(this)

    override fun generateLoadModelCypher(): String  = CosmicNCVDao(this).generateLoadCosmicModelCypher()

    override fun getModelGeneSymbol(): String = ""

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier = generateNodeIdentifierByModel(CosmicNCV, this)

    override fun getPubMedIds(): List<Int> = listOf(pubmedId)

    fun getKey():String = sampleName.plus(":").plus(genomicMutationId)

    override fun isValid(): Boolean = sampleId > 0

    companion object: CoreModelCreator{
        override val nodename = "ncv"

        fun parseCsvRecord(record: CSVRecord): CosmicNCV =
            CosmicNCV(
                record.get("Sample name"),
                record.get("ID_SAMPLE").parseValidInteger(),
                record.get("ID_tumour").parseValidInteger(),
                record.get("GENOMIC_MUTATION_ID"),
                record.get("LEGACY_MUTATION_ID"),
                record.get("zygosity"),
                record.get("GRCh").parseValidInteger(),
                record.get("genome position"),
                record.get("Mutation somatic status"),
                record.get("WT_SEQ"),
                record.get("MUT_SEQ"),
                record.get("Whole_Genome_Reseq").YNtoBoolean(),
                record.get("Whole_Exome").YNtoBoolean(),
                record.get("ID_STUDY").parseValidInteger(),
                record.get("PUBMED_PMID").parseValidInteger(),
                record.get("HGVSG")
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
            = ::parseCsvRecord
        override val nodeIdProperty: String
            get() = "genomic_mutation_id"
        override val nodelabel: String
            get() = "CosmicNCV"
    }
}