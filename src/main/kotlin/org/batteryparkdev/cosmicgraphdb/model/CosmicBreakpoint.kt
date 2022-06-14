package org.batteryparkdev.cosmicgraphdb.model

import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.neo4j.service.Neo4jUtils
import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.neo4j.driver.Value

/*
Represents the data in the CosmicBreakpointsExport files
Key: mutationId
Relationships:  Struct -[HAS_BREAKPOINT]->  Breakpoint
                Breakpoint - [HAS_PUBLICATION] -> Publication

 */
data class CosmicBreakpoint(
    val sampleName: String, val sampleId: Int, val tumorId: Int,
    val mutationType: CosmicType, val mutationId: Int,
    val chromosomeFrom: String,
    val locationFromMin: Int, val locationFromMax: Int,
    val strandFrom: String, val chromosomeTo: String, val locationToMin: Int, val locationToMax: Int,
    val strandTo: String, val pubmedId: Int = 0, val studyId: Int
 ): CosmicModel
{
    override fun getNodeIdentifier(): NodeIdentifier =
       NodeIdentifier("CosmicBreakpoint", "mutation_id", mutationId.toString())

    override fun isValid(): Boolean = (sampleId > 0).and(mutationId > 0)

    override fun getPubMedId(): Int = pubmedId

    override fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher()
        .plus(generateSampleMutationCollectionRelationshipCypher(sampleId, nodename))
        .plus(generateStructRelationshipCypher())
        .plus(" RETURN ${CosmicBreakpoint.nodename}\n")

    private fun generateMergeCypher(): String = "CALL apoc.merge.node([\"CosmicBreakpoint\"], " +
            " {mutation_id: $mutationId}, " +
            " {sample_id: $sampleId," +
            " sample_name: ${Neo4jUtils.formatPropertyValue(sampleName)}, " +
            "chromosome_from: \"$chromosomeFrom\" , " +
            " location_from_min: $locationFromMin," +
            " location_from_max: $locationFromMax, " +
            " strand_from: ${Neo4jUtils.formatPropertyValue(strandFrom)}, " +
            "chromosome_to: \"$chromosomeTo\", " +  // can't use utility here
            " location_to_min: $locationToMin," +
            " location_to_max: $locationToMax, " +
            " strand_to: ${Neo4jUtils.formatPropertyValue(strandTo)}," +
            "  pubmed_id: $pubmedId, study_id: ${studyId.toString()}," +
            " created: datetime() }," +
            " { last_mod: datetime()}) YIELD node AS ${CosmicBreakpoint.nodename} \n "


    private fun generateStructRelationshipCypher(): String =
        CosmicStruct.generateChildRelationshipCypher(mutationId,nodename)


    companion object : AbstractModel {
         const val  nodename = "breakpoint"

        fun parseCSVRecord(record: CSVRecord): CosmicBreakpoint {
            val sampleName = record.get("Sample name")
            val sampleId = record.get("ID_SAMPLE").toInt()
            val tumorId = record.get("ID_TUMOUR").toInt()
            val mutationId = record.get("Mutation ID").toInt()
            val chromFrom = record.get("Chrom From")
            val locationFromMin = record.get("Location From min").toInt()
            val locationFromMax = record.get("Location From max").toInt()
            val strandFrom = record.get("Strand From")
            val chromTo = record.get("Chrom To")
            val locationToMin = record.get("Location To min").toInt()
            val locationToMax = record.get("Location To max").toInt()
            val strandTo = record.get("Strand To")
            val pubmedId = parseValidIntegerFromString(record.get("Pubmed_PMID"))
            val studyId = parseValidIntegerFromString(record.get("ID_STUDY"))
            return CosmicBreakpoint(
                sampleName, sampleId, tumorId, CosmicType(
                    "Mutation", record.get("Mutation Type")),
                mutationId, chromFrom, locationFromMin, locationFromMax, strandFrom,
                chromTo, locationToMin, locationToMax, strandTo, pubmedId, studyId
            )
        }


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
            val pubmedId = parseValidIntegerFromString(value["Pubmed_PMID"].asString())
            val studyId = parseValidIntegerFromString(value["ID_STUDY"].asString())

            return CosmicBreakpoint(
                sampleName, sampleId, tumorId, resolveMutationType(value),
                mutationId, chromFrom, locationFromMin, locationFromMax, strandFrom,
                chromTo, locationToMin, locationToMax, strandTo, pubmedId, studyId
            )
        }


    }


}

