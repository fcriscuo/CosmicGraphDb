package org.batteryparkdev.cosmicgraphdb.cosmic.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService
import java.nio.file.Paths

data class CosmicBreakpoint(
    val sampleName: String, val sampleId: Int, val tumorId:Int,
    val site: CosmicType, val histology: CosmicType,
    val mutationType: CosmicType, val mutationId: Int,
    val chromosomeFrom: String,
    val locationFromMin: Int, val locationFromMax: Int,
    val strandFrom: String, val chromosomeTo: String, val locationToMin:Int, val locationToMax: Int,
    val strandTo: String, val pubmedId: Int= 0, val studyId: Int
) {

    companion object : AbstractModel {
        fun parseCsvRecord(record: CSVRecord):CosmicBreakpoint  =
            CosmicBreakpoint(
                record.get("Sample name"),
                record.get("ID_SAMPLE").toInt(),
                record.get("ID_TUMOUR").toInt(),
                CosmicType.resolveSiteTypeBySource(record,"CosmicBreakpoint"),
                CosmicType.resolveHistologyTypeBySource(record,"CosmicBreakpoint"),
                CosmicType.resolveBreakpointMutationType(record),
                record.get("Mutation ID").toInt(),
                // From
                record.get("Chrom From"),
                record.get("Location From min").toInt(),
                record.get("Location From max").toInt(),
                record.get("Strand From"),
                //To
                record.get("Chrom To"),
                record.get("Location To min").toInt(),
                record.get("Location To max").toInt(),
                record.get("Strand To"),
                parseValidIntegerFromString(record.get("Pubmed_PMID")),
                parseValidIntegerFromString(record.get("ID_STUDY"))
            )
    }
}

fun main () {
    //cosmic.sample.data.directory
    //file.cosmic.breakpoints.export
    val dataDirectory =  DatafilePropertiesService.resolvePropertyAsString("cosmic.sample.data.directory")
    val cosmicBreakpointsFile = dataDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.breakpoints.export")
    val path = Paths.get(cosmicBreakpointsFile)
    println("Processing COSMIC breakpoints file ${path.fileName}")
    var recordCount = 0
    TsvRecordSequenceSupplier(path).get().chunked(500)
        .forEach { it ->
            it.stream()
                .map { CosmicBreakpoint.parseCsvRecord(it) }
                .forEach { breakpoint ->
                    println(
                        "Breakpoint Mutation Id= ${breakpoint.mutationId}  " +
                                "  Tumor Id = ${breakpoint.tumorId}   PubMed Id: ${breakpoint.pubmedId}\n" +
                                "     From: ${breakpoint.chromosomeFrom}  ${breakpoint.locationFromMin } "+
                                "  ${breakpoint.locationFromMax}   ${breakpoint.strandFrom} \n" +
                                "     To: ${breakpoint.chromosomeTo}  ${breakpoint.locationToMin } "+
                                "  ${breakpoint.locationToMax}   ${breakpoint.strandTo} \n"

                    )
                    recordCount += 1
                }
        }
    println("Record count = $recordCount")
}