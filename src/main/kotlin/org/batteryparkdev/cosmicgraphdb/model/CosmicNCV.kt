package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

data class CosmicNCV(
  val sampleName: String, val sampleId: Int,val tumorId: Int,
  val genomicMutationId: String, val legacyMutationId: String,
  val zygosity: String, val grch: Int,
  val genomePosition: String, val mutationSomaticStatus: String,
  val wtSeq: String, val mutSeq: String,
  val wholeGenomeReseq: Boolean, val wholeExome:Boolean, val studyId: Int,
  val pubmedId: Int, val hgvsg: String
): CosmicModel  {

    override fun getNodeIdentifier(): NodeIdentifier = NodeIdentifier("CosmicNCV",
    "genomic_mutation_id", genomicMutationId)

    fun getKey():String = sampleName.plus(":").plus(genomicMutationId)

    override fun isValid(): Boolean = sampleId > 0
    override fun getPubMedId(): Int = pubmedId

    override fun generateLoadCosmicModelCypher(): String = generatemergeCypher()
        .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
        .plus(" RETURN  $nodename")

    private fun generatemergeCypher(): String =
        "CALL apoc.merge.node( [\"CosmicNCV\"], " +
                "{genomic_mutation_id: ${Neo4jUtils.formatPropertyValue(genomicMutationId)}}," +  // unique value for node
                " { sample_name: ${Neo4jUtils.formatPropertyValue(sampleName)}, " +
                " sample_id: $sampleId, tumor_id: $tumorId, " +
                " legacy_mutation_id: ${Neo4jUtils.formatPropertyValue(legacyMutationId)}, " +
                " zygosity: ${Neo4jUtils.formatPropertyValue(zygosity)}, " +
                " grch: $grch, genome_position: ${Neo4jUtils.formatPropertyValue(genomePosition)}, " +
                " mutation_somatic_status: ${Neo4jUtils.formatPropertyValue(mutationSomaticStatus)}, " +
                " wt_seq: ${Neo4jUtils.formatPropertyValue(wtSeq)}," +
                " mut_seq: ${Neo4jUtils.formatPropertyValue(mutSeq)}, " +
                " whole_genome_reseq: $wholeGenomeReseq, whole_exome: $wholeExome, study_id: $studyId, " +
                " pubmed_id: $pubmedId, hgvsg: ${Neo4jUtils.formatPropertyValue(hgvsg)}," +
                " created: datetime() }, { last_mod: datetime()}) YIELD node AS $nodename \n "


    companion object: AbstractModel{
        const val nodename = "ncv"

        fun parseCSVRecord(record: CSVRecord): CosmicNCV =
            CosmicNCV(
                record.get("Sample name"),
                record.get("ID_SAMPLE").toInt(),
                record.get("ID_tumour").toInt(),
                record.get("GENOMIC_MUTATION_ID"),
                record.get("LEGACY_MUTATION_ID"),
                record.get("zygosity"),
                record.get("GRCh").toInt(),
                record.get("genome position"),
                record.get("Mutation somatic status"),
                record.get("WT_SEQ"),
                record.get("MUT_SEQ"),
                convertYNtoBoolean(record.get("Whole_Genome_Reseq")),
                convertYNtoBoolean(record.get("Whole_Exome")),
                parseValidIntegerFromString(record.get("ID_STUDY")),
                parseValidIntegerFromString(record.get("PUBMED_PMID")),
                record.get("HGVSG")
            )
    }
}