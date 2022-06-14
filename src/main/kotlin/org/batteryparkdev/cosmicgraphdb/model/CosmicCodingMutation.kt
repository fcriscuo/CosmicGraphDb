package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

/*
Represents the data in the CosmicMutantExport or CosmicMutantExportCensus files
Key: mutationId
Relationships:  
   Sample - [HAS_MUTATION_COLLECTON] -> SampleMutationCollection - [HAS_CODING_MUTATION] -> CodingMutation
   Gene - [HAS_MUTATION_COLLECTION] -> GeneMutationCollection - [HAS_CODING_MUTATION] -> CodingMutation
   CodingMutation - [HAS_PUBLICATION] -> Publication

 */
data class CosmicCodingMutation(
    val geneSymbol: String, val sampleId: Int,
    val genomicMutationId: String, val geneCDSLength: Int,
    val hgncId: Int, val legacyMutationId: String,
    val mutationId: Int, val mutationCds: String,
    val mutationAA: String, val mutationDescription: String, val mutationZygosity: String,
    val LOH: String, val GRCh: String, val mutationGenomePosition: String,
    val mutationStrand: String, val resistanceMutation: String,
    //val fathmmPrediction: String, val fathmmScore: Double,
    val mutationSomaticStatus: String,
    val pubmedId: Int, val genomeWideScreen: Boolean,
    val hgvsp: String, val hgvsc: String, val hgvsg: String, val tier: String
    //val tumor: CosmicTumor
) : CosmicModel {
    override fun getNodeIdentifier(): NodeIdentifier =
        NodeIdentifier(
            "CosmicCodingMutation", "mutation_id",
            mutationId.toString()
        )

    override fun isValid(): Boolean = geneSymbol.isNotEmpty().and(sampleId>0)
    override fun getPubMedId(): Int  = pubmedId

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
        .plus(generateGeneMutationCollectionRelationshipCypher(geneSymbol, nodename))
        .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
        .plus(" RETURN $nodename")

    private fun generateMergeCypher(): String = mergeNewNodeCypher

    private val mergeNewNodeCypher = " CALL apoc.merge.node( [\"CosmicCodingMutation\"], " +
            " {mutation_id: $mutationId}, " + // key
            " { legacy_mutation_id: ${Neo4jUtils.formatPropertyValue(legacyMutationId)} ," +
            " gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}, " +
            "  gene_cds_length: $geneCDSLength, " +
            " genomic_mutation_id: ${Neo4jUtils.formatPropertyValue(genomicMutationId)} ,"+
            " mutation_cds: ${Neo4jUtils.formatPropertyValue(mutationCds)}," +
            " mutation_aa: ${Neo4jUtils.formatPropertyValue(mutationAA)}, " +
            " description: ${Neo4jUtils.formatPropertyValue(mutationDescription)}," +
            " zygosity: ${Neo4jUtils.formatPropertyValue(mutationZygosity)}, " +
            " loh: ${Neo4jUtils.formatPropertyValue(LOH)}, " +
            " grch: ${Neo4jUtils.formatPropertyValue(GRCh)}, " +
            " genome_position: ${Neo4jUtils.formatPropertyValue(mutationGenomePosition)}, " +
            " strand: ${Neo4jUtils.formatPropertyValue(mutationStrand)}, " +
            " resistance_mutation: ${Neo4jUtils.formatPropertyValue(resistanceMutation)}, " +
           // " fathmm_prediction: ${Neo4jUtils.formatPropertyValue(fathmmPrediction)}, " +
           // " fathmm_score: $fathmmScore, " +
            " somatic_status: ${Neo4jUtils.formatPropertyValue(mutationSomaticStatus)}, " +
            " pubmed_id: $pubmedId, genome_wide_screen: $genomeWideScreen, " +
            " hgvsp: ${Neo4jUtils.formatPropertyValue(hgvsp)}, " +
            " hgvsc: ${Neo4jUtils.formatPropertyValue(hgvsc)}, " +
            " hgvsq: ${Neo4jUtils.formatPropertyValue(hgvsg)}, " +
            " tier: ${Neo4jUtils.formatPropertyValue(tier)}, " +
            "  created: datetime()},{}) YIELD node as $nodename \n"

    // Cypher to complete an existing placeholder node
    private val mergeExistingNodeCypher = " CALL apoc.merge.node( [\"CosmicCodingMutation\"], " +
            " {mutation_id: $mutationId}, {}," +
            " { legacy_mutation_id: ${Neo4jUtils.formatPropertyValue(legacyMutationId)}, " +
            " gene_symbol: ${Neo4jUtils.formatPropertyValue(geneSymbol)}, " +
            " genomic_mutation_id: ${Neo4jUtils.formatPropertyValue(genomicMutationId)}, " +
            "  gene_cds_length: $geneCDSLength, " +
            " mutation_cds: ${Neo4jUtils.formatPropertyValue(mutationCds)}," +
            " mutation_aa: ${Neo4jUtils.formatPropertyValue(mutationAA)}, " +
            " description: ${Neo4jUtils.formatPropertyValue(mutationDescription)}," +
            " zygosity: ${Neo4jUtils.formatPropertyValue(mutationZygosity)}, " +
            " loh: ${Neo4jUtils.formatPropertyValue(LOH)}, " +
            " grch: ${Neo4jUtils.formatPropertyValue(GRCh)}, " +
            " genome_position: ${Neo4jUtils.formatPropertyValue(mutationGenomePosition)}, " +
            " strand: ${Neo4jUtils.formatPropertyValue(mutationStrand)}, " +
            " resistance_mutation: ${Neo4jUtils.formatPropertyValue(resistanceMutation)}, " +
           // " fathmm_prediction: ${Neo4jUtils.formatPropertyValue(fathmmPrediction)}, " +
            //" fathmm_score: $fathmmScore, " +
            " somatic_status: ${Neo4jUtils.formatPropertyValue(mutationSomaticStatus)}, " +
            " pubmed_id: $pubmedId, genome_wide_screen: $genomeWideScreen, " +
            " hgvsp: ${Neo4jUtils.formatPropertyValue(hgvsp)}, " +
            " hgvsc: ${Neo4jUtils.formatPropertyValue(hgvsc)}, " +
            " hgvsq: ${Neo4jUtils.formatPropertyValue(hgvsg)}, " +
            " tier: ${Neo4jUtils.formatPropertyValue(tier)}, " +
            "  created: datetime()}) YIELD node as $nodename \n"


    companion object : AbstractModel {
        const val nodename = "coding_mutation"

        /*
        Private function to match or create a CosmicCodingMutationNode based on the specified
        mutation id
         */
        private fun resolveCodingMutationCypher(mutationId: Int): String =
            "CALL apoc.merge.node( [\"CosmicCodingMutation\"], " +
                    " {mutation_id: $mutationId}," +
                    "  {created: datetime()},{}) " +
                    " YIELD node AS $nodename\n "

        fun generateChildRelationshipCypher(mutationId: Int, childLabel: String): String {
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_mutation"
            return resolveCodingMutationCypher(mutationId).plus(
                "CALL apoc.merge.relationship($nodename, '$relationship', " +
                        " {}, {created: datetime()}, ${childLabel.lowercase()},{} )" +
                        " YIELD rel as $relname \n"
            )
        }

        fun parseValueMap(value: Value): CosmicCodingMutation =
            CosmicCodingMutation(
                value["Gene name"].asString(), // actually HGNC approved symbol
                value["ID_sample"].asString().toInt(),
                value["GENOMIC_MUTATION_ID"].asString(),
                value["Gene CDS length"].asString().toInt(),
                value["HGNC ID"].asString().toInt(),
                value["LEGACY_MUTATION_ID"].asString(),
                value["MUTATION_ID"].asString().toInt(),
                value["Mutation CDS"].asString(),
                value["Mutation AA"].asString(),
                value["Mutation Description"].asString(),
                value["Mutation zygosity"].asString() ?: "",
                value["LOH"].asString() ?: "",
                value["GRCh"].asString() ?: "38",
                value["Mutation genome position"].asString(),
                value["Mutation strand"].asString(),
                value["Resistance Mutation"].asString(),
               // value["FATHMM prediction"].asString(),
               // parseValidDoubleFromString(value["FATHMM score"].asString()),
                value["Mutation somatic status"].asString(),
                parseValidIntegerFromString(value["Pubmed_PMID"].asString()),
                convertYNtoBoolean(value["Genome-wide screen"].asString()),
                value["HGVSP"].asString(),
                value["HGVSC"].asString(),
                value["HGVSG"].asString(),
                resolveTier(value)
                //CosmicTumor.parseValueMap(value)
            )

          /*
          Method to parse a CsvRecord into a CosmicCodingMutation
          CosmicMutantExportCensus file is too large to use an APOC method
           */
          fun parseCSVRecord(record:CSVRecord): CosmicCodingMutation =
              CosmicCodingMutation(
                  record.get("Gene name"), // actually HGNC approved symbol
                  record.get("ID_sample").toInt(),
                  record.get("GENOMIC_MUTATION_ID"),
                  parseValidIntegerFromString(record.get("Gene CDS length")),
                  parseValidIntegerFromString(record.get("HGNC ID")),
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
                  //record.get("FATHMM prediction"),
                  //parseValidDoubleFromString(record.get("FATHMM score")),
                  record.get("Mutation somatic status"),
                  parseValidIntegerFromString(record.get("Pubmed_PMID")),
                  convertYNtoBoolean(record.get("Genome-wide screen")),
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