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

    fun generateCompleteCNACypher():String =
        generateMergeCypher().plus(generateGeneRelationshipCypher())
            .plus(mutationType.generateCosmicTypeCypher(nodename))
            .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
            .plus(generateGeneMutationCollectionRelationshipCypher(geneSymbol, nodename))
            .plus(" RETURN  $nodename\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicCompleteCNA\"], " +
            " {cna_id: ${cnaId} }, "+
            " { cnv_id: ${cnvId.toString()}, " +
            " total_cn: ${totalCn.toString()}, minor_allele: ${Neo4jUtils.formatPropertyValue(minorAllele)}," +
            " study_id: ${studyId.toString()}, grch: \"$grch\"," +
            " chromosome_start_stop: \"$chromosomeStartStop\",created: datetime()  " +
            " }, { last_mod: datetime()}) YIELD node AS $nodename \n"

    /*
    Function to generate Cypher commands to create a
    CosmicGene- [HAS_CNA] -> CosmicCNA relationship
     */
    private fun generateGeneRelationshipCypher(): String =
        CosmicGeneCensus.generateGeneParentRelationshipCypher(geneSymbol,nodename)

    /*
    Function to generate the Cypher commands to create a
    Tumor - [HAS_CNA] -> CNA relationship for this CNA
     */
    private fun generateTumorRelationshipCypher(): String =
        CosmicTumor.generateChildRelationshipCypher(tumorId, nodename)

    /*
    Function to generate Cypher command to establish
    a Sample -[HAS_CNA] -> CNA relationship
     */
    private fun generateSampleRelationshipCypher(): String =
        CosmicSample.generateChildRelationshipCypher(sampleId, nodename)

    companion object: AbstractModel {

        fun parseValueMap(value: Value): CosmicCompleteCNA =
            CosmicCompleteCNA(
                UUID.randomUUID().hashCode(),   // unique identifier
                value["CNV_ID"].asString(),
                value["ID_GENE"].asString().toInt(),
                value["gene_name"].asString(),   // actually HGNC symbol
                value["ID_SAMPLE"].asString().toInt(),
                parseValidIntegerFromString(value["ID_TUMOR"].asString()),
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
