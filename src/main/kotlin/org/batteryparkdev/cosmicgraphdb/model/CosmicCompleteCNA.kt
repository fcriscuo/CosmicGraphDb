package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value
import java.util.*

data class CosmicCompleteCNA(
    val cnaId: Int,
    val cnvId:String, val geneId:Int, val geneSymbol:String, val sampleId:Int,
    val tumorId:Int,
    val sampleName:String, val totalCn:Int, val minorAllele: String,
    val mutationType: CosmicType, val studyId: Int, val grch:String= "38",
    val chromosomeStartStop:String
) :CosmicModel
{
     val nodename = "complete_cna"

override fun getNodeIdentifier(): NodeIdentifier =
    NodeIdentifier("CosmicCompleteCNA", "cna_id",
        cnaId.toString())
    override fun isValid(): Boolean = (sampleId > 0).and(cnaId > 0)
    override fun getPubMedId(): Int = 0


    override fun generateLoadCosmicModelCypher():String =
        generateMergeCypher()
            .plus(mutationType.generateCosmicTypeCypher(nodename))
            .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
            .plus(generateGeneMutationCollectionRelationshipCypher(geneSymbol, nodename))
            .plus(" RETURN  $nodename\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicCompleteCNA\"], " +
            " {cna_id: ${cnaId} }, "+
            " { cnv_id: ${cnvId.toString()}, " +
            "  tumor_id: $tumorId, " +
            " total_cn: ${totalCn.toString()}, minor_allele: ${Neo4jUtils.formatPropertyValue(minorAllele)}," +
            " study_id: ${studyId.toString()}, grch: \"$grch\"," +
            " chromosome_start_stop: \"$chromosomeStartStop\",created: datetime()  " +
            " }, { last_mod: datetime()}) YIELD node AS $nodename \n"



    companion object: AbstractModel {

        fun parseValueMap(value: Value): CosmicCompleteCNA =
            CosmicCompleteCNA(
                UUID.randomUUID().hashCode(),   // unique identifier
                value["CNV_ID"].asString(),
                value["ID_GENE"].asString().toInt(),
                value["gene_name"].asString(),   // actually HGNC symbol
                value["ID_SAMPLE"].asString().toInt(),
                value["ID_TUMOUR"].asString().toInt(),
                value["SAMPLE_NAME"].asString(),
                value["TOTAL_CN"].asString().toInt(),
                value["MINOR_ALLELE"].asString(),
                resolveMutationType(value),
                value["ID_STUDY"].asString().toInt(),
                value["GRCh"].asString(),
                value["Chromosome:G_Start..G_Stop"].asString()
            )

        private fun resolveMutationType(value: Value): CosmicType =
            CosmicType(
                "Mutation", value["MUT_TYPE"].asString()
            )

    }
}
