package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.neo4j.driver.Value
/*
Accession Number	Gene CDS length	HGNC ID
Sample name	ID_sample ID_tumour
Primary site	Site subtype 1	Site subtype 2	Site subtype 3
Primary histology	Histology subtype 1	Histology subtype 2	Histology subtype 3
Genome-wide screen	GENOMIC_MUTATION_ID	LEGACY_MUTATION_ID	MUTATION_ID
Mutation CDS	Mutation AA	Mutation Description	Mutation zygosity
LOH	GRCh	Mutation genome position	Mutation strand	Resistance
Mutation	FATHMM prediction	FATHMM score	Mutation somatic status
Pubmed_PMID	ID_STUDY
Sample Type	Tumour origin	Age	HGVSP	HGVSC	HGVSG
 */
data class CosmicBreakpoint(
    val sampleName: String, val sampleId: Int, val tumorId: Int,
    val site: CosmicType, val histology: CosmicType,
    val mutationType: CosmicType, val mutationId: Int,
    val chromosomeFrom: String,
    val locationFromMin: Int, val locationFromMax: Int,
    val strandFrom: String, val chromosomeTo: String, val locationToMin: Int, val locationToMax: Int,
    val strandTo: String, val pubmedId: Int = 0, val studyId: Int
) {
    val nodeName = "break_node"
    val breakpointId = mutationId

    fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicBreakpoint\"], " +
            " {sample_id: ${sampleId.toString()}}," +
            " { sample_name: ${Neo4jUtils.formatPropertyValue(sampleName)}, " +
            "  tumor_id: ${tumorId.toString()}, " +
            " mutation_id: ${mutationId.toString()}, " +
            "chromosome_from: \"$chromosomeFrom\" , " +
            " location_from_min: ${locationFromMin.toString()}," +
            " location_from_max: ${locationFromMax.toString()}, " +
            " strand_from: ${Neo4jUtils.formatPropertyValue(strandFrom)}, " +
            "chromosome_to: \"$chromosomeTo\", " +  // can't use utility here
            " location_to_min: ${locationToMin.toString()}," +
            " location_to_max: ${locationToMax.toString()}, " +
            " strand_to: ${Neo4jUtils.formatPropertyValue(strandTo)}," +
            "  pubmed_id: ${pubmedId.toString()}, study_id: ${studyId.toString()}," +
            " created: datetime() }," +
            " { last_mod: datetime()}) YIELD node AS $nodeName \n "


    companion object : AbstractModel {

        fun parseValueMap(value: Value): CosmicBreakpoint {
            val sampleName = value["Sample name"].asString()
            val sampleId = value["ID_SAMPLE"].asString().toInt()
            val tumorId = value["ID_TUMOUR"].asString().toInt()
            val mutationId = value["Mutation ID"].asString().toInt()
            val chromFrom = value["Chrom From"].asString()
            val locationFromMin = value["Location From min"].asString().toInt()
            val locationFromMax = value["Location From max"].asString().toInt()
            val strandFrom = value["Strand From"].asString()
            val chromTo = value["Chrom To"].asString()
            val locationToMin = value["Location To min"].asString().toInt()
            val locationToMax = value["Location To max"].asString().toInt()
            val strandTo = value["Strand To"].asString()
            val pubmedId = parseValidIntegerFromString(value["Pubmed_PMID"].asString()) ?: 0
            val studyId = parseValidIntegerFromString(value["ID_STUDY"].asString()) ?: 0

            return CosmicBreakpoint(
                sampleName, sampleId, tumorId, resolveSiteType(value),
                resolveHistologySite(value), resolveMutationType(value),
                mutationId, chromFrom, locationFromMin, locationFromMax, strandFrom,
                chromTo, locationToMin, locationToMax, strandTo, pubmedId, studyId
            )
        }


        private fun resolveSiteType(value: Value): CosmicType =
            CosmicType(
                "Site", value["Primary site"].asString(),
                value["Site subtype 1"].asString(),
                value["Site subtype 2"].asString(),
                value["Site subtype 3"].asString()
            )

        private fun resolveHistologySite(value: Value): CosmicType =
            CosmicType(
                "Histology", value["Primary histology"].asString(),
                value["Histology subtype 1"].asString(),
                value["Histology subtype 2"].asString(),
                value["Histology subtype 3"].asString()
            )

        private fun resolveMutationType(value: Value): CosmicType =
            CosmicType(
                "Mutation", value["Mutation Type"].asString()
            )
    }
}

