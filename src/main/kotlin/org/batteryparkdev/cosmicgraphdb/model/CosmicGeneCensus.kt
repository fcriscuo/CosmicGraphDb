package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.CsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService
import java.nio.file.Paths

/*
Gene Symbol,Name, Entrez GeneId,Genome Location,Tier
,Hallmark,Chr Band,Somatic,Germline,Tumour Types(Somatic),
Tumour Types(Germline),Cancer Syndrome,Tissue Type,
Molecular Genetics,Role in Cancer,Mutation Types,
Translocation Partner,Other Germline Mut,
Other Syndrome,Synonyms
 */

data class CosmicGeneCensus(
    val geneSymbol:String, val geneName:String, val entrezGeneId: String,
    val genomeLocation:String, val tier:Int=0, val hallmark:Boolean = false,
    val chromosomeBand:String, val somatic:Boolean = false, val germline: Boolean,
    val somaticTumorTypeList: List<String>, val germlineTumorTypeList: List<String>,
    val cancerSyndrome:String, val tissueTypeList:List<String>, val molecularGenetics: String,
    val roleInCancerList:List<String>, val mutationTypeList: List<String>,
    val translocationPartnerList: List<String>,
    val otherGermlineMut: String, val otherSyndromeList: List<String>, val synonymList: List<String>
) {
    companion object : AbstractModel {
        fun parseCsvRecord (record: CSVRecord): CosmicGeneCensus {
            val geneSymbol =  record.get("Gene Symbol")
            val geneName = record.get("Name")
            val entrezGeneId = record.get("Entrez GeneId")
            val genomeLocation = record.get("Genome Location")
            val tier = record.get("Tier").toInt()
            val hallmark = (!record.get("Hallmark").isNullOrEmpty() &&
                    record.get("Hallmark").lowercase() == "yes")
            val chromosomeBand = record.get("Chr Band")
            val somatic = (!record.get("Somatic").isNullOrEmpty() &&
                    record.get("Somatic").lowercase() == "yes")
            val germline = (!record.get("Germline").isNullOrEmpty() &&
                    record.get("Germline").lowercase() == "yes")
            val somaticTumorTypeList = processTumorTypes(record.get("Tumour Types(Somatic)"))
            val germlineTumorTypeList = processTumorTypes(record.get("Tumour Types(Germline)"))
            val cancerSyndrome = record.get("Cancer Syndrome")?: ""
            val tissueTypeList = parseStringOnComma(record.get("Tissue Type"))
            val molecularGenetics = record.get("Molecular Genetics") ?: ""
            val roleInCancerList = parseStringOnComma(record.get("Role in Cancer") )
            val mutationTypeList = parseStringOnComma(record.get("Mutation Types") )
            val translocationPartnerList = parseStringOnComma(record.get("Translocation Partner"))
            val otherGermlineMut = record.get("Other Germline Mut")
            val otherSyndromeList = parseStringOnSemiColon(record.get("Other Syndrome"))
            val synonymList = parseStringOnComma(record.get("Synonyms"))
            return CosmicGeneCensus( geneSymbol,geneName, entrezGeneId, genomeLocation,tier,
                hallmark, chromosomeBand, somatic, germline, somaticTumorTypeList,
                germlineTumorTypeList, cancerSyndrome, tissueTypeList, molecularGenetics,
                roleInCancerList, mutationTypeList, translocationPartnerList,
                otherGermlineMut, otherSyndromeList,synonymList
            )
        }
        /*
    Function to resolve a tumor type abbeviationd
     */
        fun processTumorTypes(tumorTypes:String):List<String>  {
            val tumorTypeList = mutableListOf<String>()
            parseStringOnComma(tumorTypes).forEach {
                tumorTypeList.add(TumorTypeService.resolveTumorType(it))
            }
            return tumorTypeList.toList()  // make List immutable
        }
    }


}

fun main() {
    val path = Paths.get("./data/cancer_gene_census.csv")
    println("Processing csv file ${path.fileName}")
    var recordCount = 0
    CsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicGeneCensus.parseCsvRecord(it) }
                .forEach {
                        cgc -> println("Gene Symbol= ${cgc.geneSymbol} " +
                        "  role in cancer = ${cgc.roleInCancerList} " +
                        "  tissue type = ${cgc.tissueTypeList} +" +
                        "  translocation partner(s) = ${cgc.translocationPartnerList}\n" +
                        "  mutation type(s) = ${cgc.mutationTypeList}    somatic tumor types = ${cgc.somaticTumorTypeList} \n" +
                        "  cancer syndrome = ${cgc.cancerSyndrome}   other syndromes= ${cgc.otherSyndromeList}\n" +
                        "  other germline mut = ${cgc.otherGermlineMut}  synonyms =  ${cgc.synonymList}")
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}