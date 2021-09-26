package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicSample
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import java.nio.file.Paths
import kotlin.math.cos

/*
 val sampleId: Int,
    val sampleName: String, val tumorId: Int, val site: CosmicType, val histology: CosmicType,
    val therapyRelationship: String, val sampleDifferentiator: String, val mutationAlleleSpecification: String,
     val msi: String, val averagePloidy: String, val wholeGeneomeScrren: String, val wholeExomeScreen: String,
     val sampleRemark: String, val drugResponse: String, val grade:String, val ageAtTumorRecurrence: Int,
    val Stage: String, val cytogenetics: String, val metastaticSite: String, val tumorSource: String,
    val tumorRemark: String, val age: Int, val ethnicity: String, val environmentalVariables: String,
    val germlineMutation: String, val therapy: String, val family: String, val normalTissueTested: String,
    val gender: String, val individualRemark: String, val nciCode: String, val sampleType: String,
    val cosmicPhenotypeId: String

     sample_id	sample_name	id_tumour	id_individual
     primary_site	site_subtype_1	site_subtype_2	site_subtype_3
     primary_histology	histology_subtype_1	histology_subtype_2	histology_subtype_3
     therapy_relationship	sample_differentiator	mutation_allele_specification
     msi	average_ploidy	whole_genome_screen	whole_exome_screen	sample_remark
     drug_response	grade	age_at_tumour_recurrence	stage	cytogenetics
     metastatic_site	tumour_source	tumour_remark	age	ethnicity
     environmental_variables	germline_mutation	therapy	family
     normal_tissue_tested	gender	individual_remark	nci_code
     sample_type	cosmic_phenotype_id


 */
object CosmicSampleLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun processCosmicSampleNode(cosmicSample: CosmicSample){
        val id = loadCosmicSample(cosmicSample)
        if (cosmicSample.sampleType.isNotEmpty()) {
            addCosmicSampleTypeLabel(id, cosmicSample.sampleType)
        }
        loadCosmicSampleTypes(cosmicSample)
        createCosmicSampleRelationships(cosmicSample)
    }

    private fun loadCosmicSample(cosmicSample:CosmicSample):Int =
        Neo4jConnectionService.executeCypherCommand(
            "MERGE (cs:CosmicSample{sample_id: ${cosmicSample.sampleId}}) " +
                    " SET cs.sample_name = \"${cosmicSample.sampleName}\", " +
                    " cs.tumor_id  = ${cosmicSample.tumorId}, " +
                    " cs.therapy_relationship = \"${cosmicSample.therapyRelationship}\", " +
                    " cs.sample_differentiation = \"${cosmicSample.sampleDifferentiator}\", " +
                    " cs.mutation_allele_specification = \"${cosmicSample.mutationAlleleSpecification}\", " +
                    " cs.msi = \"${cosmicSample.msi}\", cs.average_ploidy = \"${cosmicSample.averagePloidy}\", " +
                    " cs.whole_genome_screen = \"${cosmicSample.wholeGeneomeScreen}\", " +
                    " cs.whole_exome_screen = \"${cosmicSample.wholeExomeScreen}\", " +
                    " cs.sample_remark = \"${cosmicSample.sampleRemark}\", " +
                    " cs.drug_response = \"${cosmicSample.drugResponse}\", cs.grade= \"${cosmicSample.grade}\", " +
                    " cs.age_at_tumor_recurrennce = ${cosmicSample.ageAtTumorRecurrence}, " +
                    " cs.stage=\"${cosmicSample.stage}\", cs.cytogenetics= \"${cosmicSample.cytogenetics}\", " +
                    " cs.metastatic_site = \"${cosmicSample.metastaticSite}\", " +
                    " cs.tumor_source= \"${cosmicSample.tumorSource}\", cs.tumor_remark=\"${cosmicSample.tumorRemark}\"," +
                    " cs.age=${cosmicSample.age}, cs.ethnicity=\"${cosmicSample.ethnicity}\"," +
                    " cs.environmental_variables=\"${cosmicSample.environmentalVariables}\"," +
                    " cs.germline_mutation=\"${cosmicSample.germlineMutation}\", " +
                    " cs.therapy=\"${cosmicSample.therapy}\",cs.family=\"${cosmicSample.family}\", " +
                    " cs.normal_tissue_tested=\"${cosmicSample.normalTissueTested}\", " +
                    " cs.gender=\"${cosmicSample.gender}\", cs.individual_remark=\"${cosmicSample.individualRemark}\"," +
                    " cs.nci_code=\"${cosmicSample.nciCode}\", cs.sample_type=\"${cosmicSample.sampleType}\" " +
                    " RETURN cs.sample_id").toInt()

    private fun addCosmicSampleTypeLabel(id: Int, label: String) {
        val labelExistsQuery = "MERGE (cs:CosmicSample{sample_id:$id}) " +
                "RETURN apoc.label.exists(cs, \"$label\") AS output;"
        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
            Neo4jConnectionService.executeCypherCommand(
                "MATCH (cs:CosmicSample{sample_id:$id}) " +
                        "CALL apoc.create.addLabels(cs,[\"$label\"]) YIELD node RETURN node"
            )
        }
    }
    private fun loadCosmicSampleTypes(cosmicSample: CosmicSample){
        CosmicTypeLoader.processCosmicTypeNode(cosmicSample.site)
        CosmicTypeLoader.processCosmicTypeNode(cosmicSample.histology)
    }
    private fun createCosmicSampleRelationships(cosmicSample: CosmicSample){
        // CosmicSample to CosmicClassification relationship
        Neo4jConnectionService.executeCypherCommand(
            "MATCH (cs:CosmicSample), (cc:CosmicClassification) WHERE " +
                    " cs.sample_id=${cosmicSample.sampleId} AND cc.phenotype_id = " +
                    "\"${cosmicSample.cosmicPhenotypeId}\" " +
                    " MERGE (cs) - [r:HAS_CLASSIFICATION] -> (cc)"
        )
    }

}
fun main() {
    val path = Paths.get("./data/sample_CosmicSample.tsv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicSample.parseCsvRecord(it) }
                .forEach { sample ->
                    CosmicSampleLoader.processCosmicSampleNode(sample)
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}