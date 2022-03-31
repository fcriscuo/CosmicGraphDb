package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.TsvRecordSequenceSupplier
import java.nio.file.Paths

/*
CNV_ID	ID_GENE	gene_name	ID_SAMPLE	ID_TUMOUR
Primary site Site subtype 1	Site subtype 2	Site subtype 3
Primary histology	Histology subtype 1	Histology subtype 2	Histology subtype 3
SAMPLE_NAME	TOTAL_CN	MINOR_ALLELE	MUT_TYPE
ID_STUDY	GRCh	Chromosome:G_Start..G_Stop
*/
data class CosmicCompleteCNA(
    val cnvId:Int, val geneId:Int, val geneSymbol:String, val sampleId:Int,
    val tumorId:Int,  val site: CosmicType, val histology: CosmicType,
    val sampleName:String, val totalCn:Int, val minorAllele: String,
    val mutationType: String, val studyId: Int, val grch:String= "38",
    val chromosomeStartStop:String
) {

    companion object: AbstractModel {
        fun parseCsvRecord(record: CSVRecord): CosmicCompleteCNA =
            CosmicCompleteCNA(
                record.get("CNV_ID").toInt(),
                record.get("ID_GENE").toInt(),
                record.get("gene_name"),  // actually gene symbol
                record.get("ID_SAMPLE").toInt(),
                record.get("ID_TUMOUR").toInt(),
                CosmicType.resolveSiteTypeBySource(record, "CosmicCNA"),
                CosmicType.resolveHistologyTypeBySource(record, "CosmicCNA"),
                record.get("SAMPLE_NAME"),
                record.get("TOTAL_CN").toInt(),
                record.get("MINOR_ALLELE"),
                record.get("MUT_TYPE"),
                record.get("ID_STUDY").toInt(),
                record.get("GRCh"),
                record.get("Chromosome:G_Start..G_Stop")
            )
    }
}
// basic integration test
 fun main(arges: Array<String>) {
     val path = Paths.get("./data/sample_CosmicCompleteCNA.tsv")
     println("Processing tsv file ${path.fileName}")
     TsvRecordSequenceSupplier(path).get().chunked(500)
         .forEach { it ->
             it.stream()
                 .map { CosmicCompleteCNA.parseCsvRecord(it) }
                 .forEach { cna ->
                     println(
                         "CNA gene: ${cna.geneSymbol}  total cn: ${cna.totalCn}" +
                                 " histology: ${cna.histology.primary} " +
                                 "mutation type: ${cna.mutationType}  start/stop: ${cna.chromosomeStartStop}"
                     )
                 }
         }
 }