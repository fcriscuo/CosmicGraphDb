package org.batteryparkdev.cosmicgraphdb.neo4j.dao

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicDiffMethylation
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService

private val logger: FluentLogger = FluentLogger.forEnclosingClass();
/// n.b. chromosome values are numeric (x=23, y=24)

private const val methylationLoadTemplate = "MERGE (cm: CosmicDiffMethylation{fragment_id:\"FRAGMENTID\"}) " +
        " SET cm.study_id = STUDYID, cm.sample_id= SAMPLEID, cm.tumor_id=TUMORID, " +
        " cm.genome_version=\"GENOME_VERSION\", cm.chromosome = CHROMOSOME, cm.position=POSITION, " +
        " cm.strand=\"STRAND\",cm.gene_name=\"GENENAME\",cm.methylation=\"METHYLATION\"," +
        " cm.avg_beta_value_normal=AVGBETA, cm.beta_value=BETAVALUE, cm.two_sided_pvalue = " +
        "PVALUE  RETURN cm.fragment_id"

fun createHistologyTypeRelationship(fragmentId: String, typeId: Int) =
    Neo4jConnectionService.executeCypherCommand(
        "MATCH (cm:CosmicDiffMethylation), (ct:CosmicType) WHERE " +
                "  cm.fragment_id=\"$fragmentId\" AND ct.type_id = " +
                " $typeId MERGE " +
                " (cm) -[r:HAS_HISTOLOGY] -> (ct)"
    )

fun loadCosmicDiffMethylation(methyl: CosmicDiffMethylation): String =
    Neo4jConnectionService.executeCypherCommand(
        methylationLoadTemplate.replace("FRAGMENTID", methyl.fragmentId)
            .replace("STUDYID", methyl.studyId.toString())
            .replace("SAMPLEID", methyl.sampleId.toString())
            .replace("TUMORID", methyl.tumorId.toString())
            .replace("GENOME_VERSION", methyl.genomeVersion)
            .replace("CHROMOSOME", methyl.chromosome.toString())
            .replace("POSITION", methyl.position.toString())
            .replace("STRAND", methyl.strand)
            .replace("GENENAME", methyl.geneName)
            .replace("METHYLATION", methyl.methylation)
            .replace("AVGBETA", methyl.avgBetaValueNormal.toString())
            .replace("BETAVALUE", methyl.betaValue.toString())
            .replace("PVALUE", methyl.twoSidedPValue.toString())
    )

fun createSampleRelationship(sampleId: Int, fragmentId: String) =
    Neo4jConnectionService.executeCypherCommand(
        "" +
                "MATCH (cs:CosmicSample), (cm:CosmicDiffMethylation)" +
                " WHERE cs.sample_id=$sampleId AND " +
                "  cm.fragment_id = \"$fragmentId\" " +
                "  MERGE (cs) - [r:HAS_METHYLATION]-> (cm)"
    )
