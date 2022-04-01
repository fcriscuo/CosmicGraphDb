package org.batteryparkdev.cosmicgraphdb.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.model.CosmicDiffMethylation
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils

private val logger: FluentLogger = FluentLogger.forEnclosingClass();
/// n.b. chromosome values are numeric (x=23, y=24)

private const val methylationLoadTemplate = "MERGE (cm: CosmicDiffMethylation{fragment_id:FRAGMENTID}) " +
        " SET cm += {study_id: STUDYID, sample_id: SAMPLEID, tumor_id: TUMORID, " +
        " genome_version: GENOME_VERSION, chromosome: CHROMOSOME, position: POSITION, " +
        " strand: STRAND,gene_name: GENENAME, methylation: METHYLATION," +
        " avg_beta_value_normal: AVGBETA, beta_value: BETAVALUE, two_sided_pvalue: " +
        "PVALUE } " +
        " RETURN cm.fragment_id"

fun createHistologyTypeRelationship(fragmentId: String, typeId: Int) =
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cm:CosmicDiffMethylation), (ct:CosmicType) WHERE " +
                "  cm.fragment_id=\"$fragmentId\" AND ct.type_id = " +
                " $typeId MERGE " +
                " (cm) -[r:HAS_HISTOLOGY] -> (ct)"
    )



fun loadCosmicDiffMethylation(methyl: CosmicDiffMethylation): String =
    Neo4jConnectionService.executeCypherCommand(
        methylationLoadTemplate.replace("FRAGMENTID", Neo4jUtils.formatQuotedString(methyl.fragmentId))
            .replace("STUDYID", methyl.studyId.toString())
            .replace("SAMPLEID", methyl.sampleId.toString())
            .replace("TUMORID", methyl.tumorId.toString())
            .replace("GENOME_VERSION", methyl.genomeVersion)
            .replace("CHROMOSOME", methyl.chromosome.toString())
            .replace("POSITION", methyl.position.toString())
            .replace("STRAND", Neo4jUtils.formatQuotedString(methyl.strand))
            .replace("GENENAME", Neo4jUtils.formatQuotedString(methyl.geneName))
            .replace("METHYLATION", Neo4jUtils.formatQuotedString(methyl.methylation))
            .replace("AVGBETA", methyl.avgBetaValueNormal.toString())
            .replace("BETAVALUE", methyl.betaValue.toString())
            .replace("PVALUE", methyl.twoSidedPValue.toString())
    )

fun createSampleRelationship(sampleId: Int, fragmentId: String) =
    Neo4jConnectionService.executeCypherCommand(
                "MATCH (cs:CosmicSample), (cm:CosmicDiffMethylation)" +
                " WHERE cs.sample_id=$sampleId AND " +
                "  cm.fragment_id = \"$fragmentId\" " +
                "  MERGE (cs) - [r:HAS_METHYLATION]-> (cm)"
    )
