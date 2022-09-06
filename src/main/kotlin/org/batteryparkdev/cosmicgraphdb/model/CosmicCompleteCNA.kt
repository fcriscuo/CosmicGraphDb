package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.dao.CosmicCompleteCNADao
import org.batteryparkdev.cosmicgraphdb.dao.resolveMutationType
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier

data class CosmicCompleteCNA(
    val cnaId: String,  // generated value for uniqueness
    val cnvId:Int, val geneId:Int, val geneSymbol:String, val sampleId:Int,
    val tumorId:Int,
    val sampleName:String, val totalCn:Int, val minorAllele: String,
    val mutationType: CosmicType, val studyId: Int, val grch:String= "38",
    val chromosomeStartStop:String
) : CoreModel
{

    override fun generateLoadModelCypher(): String = CosmicCompleteCNADao(this).generateLoadCosmicCompleteCNACypher()

    override fun getModelGeneSymbol(): String  = geneSymbol

    override fun getModelSampleId(): String = sampleId.toString()

    override fun getNodeIdentifier(): NodeIdentifier =
    NodeIdentifier("CosmicCompleteCNA", "cna_id",
        cnaId)

    override fun getPubMedIds(): List<Int> = emptyList()

    override fun isValid(): Boolean = (sampleId > 0).and(cnvId > 0).and(geneId>0)

    companion object: CoreModelCreator{
        val nodename = "complete_cna"

        fun parseCsvRecord(record: CSVRecord): CosmicCompleteCNA =
            CosmicCompleteCNA(
                record.get("CNV_ID")
                    .plus(":")
                    .plus(record.get("ID_GENE"))
                    .plus(":")
                    .plus(record.get("ID_SAMPLE")) ,   // unique identifier
                record.get("CNV_ID").toInt(),
                record.get("ID_GENE").toInt(),
                record.get("gene_name"),   // actually HGNC symbol
                record.get("ID_SAMPLE").toInt(),
                record.get("ID_TUMOUR").toInt(),
                record.get("SAMPLE_NAME"),
                record.get("TOTAL_CN").toInt(),
                record.get("MINOR_ALLELE"),
                resolveMutationType(record),
                record.get("ID_STUDY").toInt(),
                record.get("GRCh"),
                record.get("Chromosome:G_Start..G_Stop")
            )

        override val createCoreModelFunction: (CSVRecord) -> CoreModel
             = ::parseCsvRecord
    }
}
