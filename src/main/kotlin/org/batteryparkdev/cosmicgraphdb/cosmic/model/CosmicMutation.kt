package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import java.nio.file.Paths
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier

/*
GENOMIC_MUTATION_ID		MUTATION_ID
	Mutation CDS	Mutation AA	Mutation Description	Mutation zygosity
	LOH	GRCh	Mutation genome position	Mutation strand
	SNP	Resistance Mutation	FATHMM prediction	FATHMM score
	Mutation somatic status	Pubmed_PMID	ID_STUDY	Sample Type	Tumour origin
			Tier	HGVSP	HGVSC	HGVSG
 */
data class CosmicMutation (
        val genomicMutationId:String, val mutationId:String, val mutationCds: String,
        val mutationAA: String, val mutationDescription:String, val mutationZygosity: String,
        val LOH: String, val GRCh: Int, val mutationGenomePosition: String,
        val mutationStrand: String, val SNP:Boolean, val resistanceMutation: String,
        val fathmmPrediction: String, val fathmmScore:Double, val mutationSomaticStatus: String,
         val hgvsp: String, val hgvsc: String, val hgvsg: String, val tier:String
        ) {
        companion object: AbstractModel {
            fun parseCsvRecord(record: CSVRecord): CosmicMutation =
                CosmicMutation(
                    record.get("GENOMIC_MUTATION_ID"), record.get("MUTATION_ID"),
                    record.get("Mutation CDS"), record.get("Mutation AA"),
                    record.get("Mutation Description"), record.get("Mutation zygosity") ?: "",
                    record.get("LOH") ?: "", record.get("GRCh").toInt(),
                    record.get("Mutation genome position"), record.get("Mutation strand"),
                    record.get("SNP").lowercase() == "y",
                    record.get("Resistance Mutation"), record.get("FATHMM prediction"),
                    parseValidDoubleFromString(record.get("FATHMM score")), record.get("Mutation somatic status"),
                     record.get("HGVSP"), record.get("HGVSC"),
                    record.get("HGVSG"), resolveTier(record)
                )

            /*
            Not all mutation files have a Tier column
             */
          fun resolveTier(record: CSVRecord):String =
              when(record.isMapped("Tier")) {
                  true -> record.get("Tier")
                  false -> ""
          }
        }
}
fun main() {
    val path = Paths.get("./data/sample_CosmicMutantExport.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicMutation.parseCsvRecord(it) }
                .forEach { mut ->
                    println(
                        "Cosmic Muattaion Id= ${mut.genomicMutationId}  location= ${mut.mutationGenomePosition}" +
                                "  mutation AA = ${mut.mutationAA} " +
                                "  description = ${mut.mutationDescription} "
                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}