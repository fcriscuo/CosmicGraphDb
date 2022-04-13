package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.neo4j.driver.Value

data class CosmicCompleteCNA(
    val cnvId:Int, val geneId:Int, val geneSymbol:String, val sampleId:Int,
    val tumorId:Int, val site: CosmicType, val histology: CosmicType,
    val sampleName:String, val totalCn:Int, val minorAllele: String,
    val mutationType: CosmicType, val studyId: Int, val grch:String= "38",
    val chromosomeStartStop:String
) {
     val nodeName = "cna_node"

    fun generateCompleteCNACypher():String =
        generateMergeCypher().plus(generateGeneRelationshipCypher())
            .plus(site.generateCosmicTypeCypher(nodeName))
            .plus(histology.generateCosmicTypeCypher(nodeName))
            .plus(mutationType.generateCosmicTypeCypher(nodeName))
            .plus(generateTumorRelationshipCypher())
            .plus(generateSampleRelationshipCypher())
            .plus(" RETURN node as $nodeName\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicCompleteCNA\"], " +
            " { cnv_id: ${cnvId.toString()},  " +
            " total_cn: ${totalCn.toString()}, minor_allele: ${Neo4jUtils.formatPropertyValue(minorAllele)}," +
            " study_id: ${studyId.toString()}, grch: \"$grch\"," +
            " chromosome_start_stop: \"$chromosomeStartStop\",created: datetime()  " +
            " } { last_mod: datetime()}) YIELD node AS $nodeName \n"

    /*
    Function to generate Cypher commands to create a
    CNA - [HAS_GENE] -> Gene relationship
     */
    private fun generateGeneRelationshipCypher(): String =
        CosmicGeneCensus.generateHasGeneRelationshipCypher(geneSymbol,nodeName)

    /*
    Function to generate the Cypher commands to create a
    Tumor - [HAS_CNA] -> CNA relationship for this CNA
     */
    private fun generateTumorRelationshipCypher(): String =
        CosmicTumor.generateChildRelationshipCypher(tumorId, nodeName)

    /*
    Function to generate Cypher command to establish
    a Sample -[HAS_CNA] -> CNA relationship
     */
    private fun generateSampleRelationshipCypher(): String =
        CosmicSample.generateChildRelationshipCypher(sampleId, nodeName)

    companion object: AbstractModel {

        fun parseValueMap(value: Value): CosmicCompleteCNA {
            val cnvId = value["CNV_ID"].asString().toInt()
            val geneId = value["ID_GENE"].asString().toInt()
            val geneSymbol = value["gene_name"].asString()   // actually HGNC symbol
            val sampleId = value["ID_SAMPLE"].asString().toInt()
            val tumorId = parseValidIntegerFromString(value["ID_TUMOR"].asString())
            val sampleName = value["SAMPLE_NAME"].asString()
            val totalCn = value["TOTAL_CN"].asString().toInt()
            val minorAllele = value["MINOR_ALLELE"].asString()
            val studyId = value["ID_STUDY"].asString().toInt()
            val grch = value["GRCh"].asString()
            val chrmstartstop = value["Chromosome:G_Start..G_Stop"].asString()
            return CosmicCompleteCNA(cnvId, geneId, geneSymbol, sampleId,
                tumorId, resolveSiteType(value), resolveHistologySite(value),
                sampleName, totalCn, minorAllele, resolveMutationType(value),
                studyId, grch, chrmstartstop)
        }

        private fun resolveSiteType(value: Value): CosmicType =
            CosmicType(
                "Site", value["Primary site"].asString(),
                value["Site subtype 1"].asString(),
                value["Site subtype 2"].asString(),
                value["Site subtype 3"].asString()
            )

        private fun resolveHistologySite(value: Value): CosmicType =
            CosmicType(
                "Histology", value["Primary histology"].asString(),
                value["Histology subtype 1"].asString(),
                value["Histology subtype 2"].asString(),
                value["Histology subtype 3"].asString()
            )

        private fun resolveMutationType(value: Value): CosmicType =
            CosmicType(
                "Mutation", value["MUT_TYPE"].asString()
            )

    }
}
