package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService
import java.nio.file.Paths

data class CosmicSample(
    val sampleId: Int,
    val sampleName: String,
    val tumorId: Int,
    val site: CosmicType,
    val histology: CosmicType,
    val therapyRelationship: String,
    val sampleDifferentiator: String,
    val mutationAlleleSpecification: String,
    val msi: String,
    val averagePloidy: String,
    val wholeGeneomeScreen: String,
    val wholeExomeScreen: String,
    val sampleRemark: String,
    val drugResponse: String,
    val grade:String,
    val ageAtTumorRecurrence: Int,
    val stage: String,
    val cytogenetics: String,
    val metastaticSite: String,
    val tumorSource: String,
    val tumorRemark: String,
    val age: Int,
    val ethnicity: String,
    val environmentalVariables: String,
    val germlineMutation: String,
    val therapy: String,
    val family: String,
    val normalTissueTested: String,
    val gender: String,
    val individualRemark: String,
    val nciCode: String,
    val sampleType: String,
    val cosmicPhenotypeId: String
    )
{
    companion object : AbstractModel {

        /*

        Sample name	ID_sample
        ID_tumour

         */

        fun parseCsvRecord(record: CSVRecord): CosmicSample =
            CosmicSample(
                record.get("sample_id").toInt(), record.get("sample_name"), record.get("id_tumour").toInt(),
                CosmicType.resolveSiteTypeBySource(record,"CosmicSample"),
                CosmicType.resolveHistologyTypeBySource(record,"CosmicSample"),
                record.get("therapy_relationship"), record.get("sample_differentiator"),
                record.get("mutation_allele_specification"), record.get("msi"), record.get("average_ploidy"),
                record.get("whole_genome_screen"),record.get("whole_exome_screen"),
                removeInternalQuotes(record.get("sample_remark")),
                record.get("drug_response"), record.get("grade"),
                parseValidIntegerFromString(record.get("age_at_tumour_recurrence")),
                record.get("stage"), record.get("cytogenetics"), record.get("metastatic_site"),
                record.get("tumour_source"),
                removeInternalQuotes(record.get("tumour_remark")),
                parseValidIntegerFromString(record.get("age")),
                record.get("ethnicity"), record.get("environmental_variables"), record.get("germline_mutation"),
                record.get("therapy"), record.get("family"), record.get("normal_tissue_tested"),
                record.get("gender"), record.get("individual_remark"), record.get("nci_code"),
                record.get("sample_type"), record.get("cosmic_phenotype_id")
            )
    }
}

fun main() {
    /* scan complete file to diagnose any data parsing issues */
   val dataDirectory =  DatafilePropertiesService.resolvePropertyAsString("cosmic.data.directory")
    val cosmicSampleFile = dataDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.sample")
    val path = Paths.get(cosmicSampleFile)
    println("Processing COSMIC sample file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicSample.parseCsvRecord(it) }
                .forEach { sample ->
                    println(
                        "Sample Id= ${sample.sampleId}  SampleType= ${sample.sampleType}" +
                                "  Tumor Id = ${sample.tumorId} " +
                                "  sample name = ${sample.sampleName}" +
                                "  age = ${sample.age} " +
                                "  Tumor Source = ${sample.tumorSource} " +
                                "  Germline Mutation = ${sample.germlineMutation} "

                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}