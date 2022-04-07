package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import java.nio.file.Paths
import org.batteryparkdev.io.TsvRecordSequenceSupplier

data class CosmicMutation(
    val geneSymbol: String,
    val genomicMutationId: String, val mutationId: Int, val mutationCds: String,
    val mutationAA: String, val mutationDescription: String, val mutationZygosity: String,
    val LOH: String, val GRCh: String, val mutationGenomePosition: String,
    val mutationStrand: String,  val resistanceMutation: String,
    val fathmmPrediction: String, val fathmmScore: Double, val mutationSomaticStatus: String,
    val pubmedId: Int,
    val hgvsp: String, val hgvsc: String, val hgvsg: String, val tier: String

) {

    companion object : AbstractModel {
        const val nodename = "mutation"
       \

        private fun generateMutationPlaceholderCypher(mutationId: Int): String =
            "CALL apoc.merge.node( [\"CosmicMutation\"], " +
                    " {mutation_id $mutationId,  created: datetime()} " +
                    " YIELD node AS ${CosmicMutation.nodename}"

        fun generateChildRelationshipCypher(mutationId: Int, childLabel: String): String {
            val relationship = "HAS_".plus(childLabel.uppercase())
            val relname = "rel_mutation"
            return generateMutationPlaceholderCypher(mutationId).plus(
                "CALL apoc.merge.relationship(${CosmicMutation.nodename}, $relationship, " +
                        " {}, {created: datetime()}, $childLabel,{} " +
                        " YIELD rel as $relname \n"
            )
        }

        fun parseCsvRecord(record: CSVRecord): CosmicMutation =
            CosmicMutation(
                record.get("Gene name"),   // actually HGNC approved symbol
                record.get("GENOMIC_MUTATION_ID"), record.get("MUTATION_ID").toInt(),
                record.get("Mutation CDS"),
                record.get("Mutation AA"),
                record.get("Mutation Description"),
                record.get("Mutation zygosity") ?: "",
                record.get("LOH") ?: "", record.get("GRCh")?:"38",
                record.get("Mutation genome position"),
                record.get("Mutation strand"),
                record.get("Resistance Mutation"), record.get("FATHMM prediction"),
                parseValidDoubleFromString(record.get("FATHMM score")), record.get("Mutation somatic status"),
                parseValidIntegerFromString(record.get("Pubmed_PMID")),
                record.get("HGVSP"), record.get("HGVSC"),
                record.get("HGVSG"), resolveTier(record)
            )

        /*
        The CosmicResistanceMutations file has a limited number of mutation parameters.
        But they represent the more critical mutation parameters
         */
        fun parseResistanceMutationCsvRecord(record: CSVRecord): CosmicMutation =
            CosmicMutation(
                record.get("Gene Name"),
                record.get("GENOMIC_MUTATION_ID"), record.get("MUTATION_ID").toInt(),
                record.get("CDS Mutation"),
                record.get("AA Mutation"), "",
                record.get("Zygosity") ?: "",
                "", "38",
                record.get("Genome Coordinates (GRCh38)"), "", "Y",
                "", 0.0, "",
                parseValidIntegerFromString(record.get("Pubmed_PMID")),
                record.get("HGVSP"), record.get("HGVSC"),
                record.get("HGVSG"), resolveTier(record)
            )

        /*
        Not all mutation files have a Tier column
         */
        fun resolveTier(record: CSVRecord): String =
            when (record.isMapped("Tier")) {
                true -> record.get("Tier")
                false -> ""
            }
    }
}

fun main() {
    val path = Paths.get("./data/sample_CosmicMutantExportCensus.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicMutation.parseCsvRecord(it) }
                .forEach { mut ->
                    println(
                        "Cosmic Muattaion Id= ${mut.mutationId}  location= ${mut.mutationGenomePosition}" +
                                "  mutation AA = ${mut.mutationAA} " +
                                "  description = ${mut.mutationDescription} " +
                                "  gene = ${mut.geneSymbol} " +
                                "  pubmed id: ${mut.pubmedId} "
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}