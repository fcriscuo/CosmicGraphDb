package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicNCV(
  val sampleName: String, val sampleId: Int,val tumorId: Int,
  val genomicMutationId: String, val legacyMutationId: String,
  val zygosity: String, val grch: Int,
  val genomePosition: String, val mutationSomaticStatus: String,
  val wtSeq: String, val mutSeq: String,
  val fathmmMklNonCodingScore: Double, val fathmmMklNonCodingGroups:String,
  val fathmmMklCodingScore: Double, val fathmmMklCodingGroups:String,
  val wholeGenomeReseq: Boolean, val wholeExome:Boolean, val studyId: Int,
  val pubmedId: Int, val hgvsg: String
): CosmicModel  {

    override fun getNodeIdentifier(): NodeIdentifier = NodeIdentifier("CosmicNCV",
    "sample_name", sampleName)

    fun getKey():String = sampleName.plus(":").plus(genomicMutationId)

    fun generateCosmicNCVCypher(): String = generatemergeCypher()
        .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
        .plus(" RETURN  $nodename")

    private fun generatemergeCypher(): String =
        "CALL apoc.merge.node( [\"CosmicNCV\"], " +
                "{key: ${Neo4jUtils.formatPropertyValue(getKey())}}," +  // unique value for node
                " { sample_name: ${Neo4jUtils.formatPropertyValue(sampleName)}, " +
                " sample_id: $sampleId, tumor_id: $tumorId, " +
                " genomic_mutation_id: ${Neo4jUtils.formatPropertyValue(genomicMutationId)}, " +
                " legacy_mutation_id: ${Neo4jUtils.formatPropertyValue(legacyMutationId)}, " +
                " zygosity: ${Neo4jUtils.formatPropertyValue(zygosity)}, " +
                " grch: $grch, genome_position: ${Neo4jUtils.formatPropertyValue(genomePosition)}, " +
                " mutation_somatic_status: ${Neo4jUtils.formatPropertyValue(mutationSomaticStatus)}, " +
                " wt_seq: ${Neo4jUtils.formatPropertyValue(wtSeq)}," +
                " mut_seq: ${Neo4jUtils.formatPropertyValue(mutSeq)}, " +
                " fathmm_mlk_noncoding_score: $fathmmMklNonCodingScore, " +
                " fathmm_mlk_noncoding_groups: ${Neo4jUtils.formatPropertyValue(fathmmMklNonCodingGroups)}, " +
                " fathmm_mlk_coding_score: $fathmmMklCodingScore, " +
                " fathmm_mlk_coding_groups: ${Neo4jUtils.formatPropertyValue(fathmmMklCodingGroups)}, " +
                " whole_genome_reseq: $wholeGenomeReseq, whole_exome: $wholeExome, study_id: $studyId, " +
                " pubmed_id: $pubmedId, hgvsg: ${Neo4jUtils.formatPropertyValue(hgvsg)}," +
                " created: datetime() }, { last_mod: datetime()}) YIELD node AS $nodename \n "


    companion object: AbstractModel{
        const val nodename = "ncv"
        fun parseValueMap(value: Value): CosmicNCV =
            CosmicNCV(
                value["Sample name"].asString(),
                value["ID_SAMPLE"].asString().toInt(),
                value["ID_tumour"].asString().toInt(),
                value["GENOMIC_MUTATION_ID"].asString(),
                value["LEGACY_MUTATION_ID"].asString(),
                value["zygosity"].asString(),
                value["GRCh"].asString().toInt(),
                value["genome position"].asString(),
                value["Mutation somatic status"].asString(),
                value["WT_SEQ"].asString(),
                value["MUT_SEQ"].asString(),
                parseValidDoubleFromString(value["FATHMM_MKL_NON_CODING_SCORE"].asString()),
                value["FATHMM_MKL_NON_CODING_GROUPS"].asString(),
                parseValidDoubleFromString(value["FATHMM_MKL_CODING_SCORE"].asString()),
                value["FATHMM_MKL_CODING_GROUPS"].asString(),
                convertYNtoBoolean(value["Whole_Genome_Reseq"].asString()),
                convertYNtoBoolean(value["Whole_Exome"].asString()),
                parseValidIntegerFromString(value["ID_STUDY"].asString()),
                parseValidIntegerFromString(value["PUBMED_PMID"].asString()),
                value["HGVSG"].asString()
            )
    }
}