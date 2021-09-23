package org.batteryparkdev.cosmicgraphdb.neo4j.loader

import com.google.common.flogger.FluentLogger
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicGene
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicGeneCensus
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jUtils
import org.batteryparkdev.cosmicgraphdb.service.TumorTypeService
import java.util.*

/*
Responsible for creating/merging a CosmicGene node

private const val mergePubMedArticleTemplate = "MERGE (pma:PubMedArticle { pubmed_id: PMAID}) " +
            "SET  pma.pmc_id = \"PMCID\", pma.doi_id = \"DOIID\", " +
            " pma.journal_name = \"JOURNAL_NAME\", pma.journal_issue = \"JOURNAL_ISSUE\", " +
            " pma.article_title = \"TITLE\", pma.abstract = \"ABSTRACT\", " +
            " pma.author = \"AUTHOR\", pma.reference_count = REFCOUNT, " +
            " pma.cited_by_count = CITED_BY " +
            "  RETURN pma.pubmed_id"

             val geneSymbol:String, val geneName:String, val entrezGeneId: String,
    val genomeLocation:String, val Tier:Int=0, val hallmark:Boolean = false,
    val chromosomeBand:String, val somatic:Boolean = false, val germline: Boolean,
    val somaticTumorTypeList: List<String>, val germlineTumorTypeList: List<String>,
    val cancerSyndrome:String, val tissueTypeList:List<String>, val molecularGenetics: String,
    val roleInCancerList:List<String>, val mutationTypeList: List<String>,
    val translocationPartnerList: List<String>,
    val otherGermlineMut: String, val otherSyndromeList: List<String>, val synonyms: String
 */
object CosmicGeneLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();
    private const val cypherLoadTemplate = "MERGE (cg:CosmicGene{gene_symbol: \"GENESYMBOL\" }) " +
            "SET gene_name = \"GENENAME\", entrez_gene_id = \"ENTREZ\", genome_location = \"LOCATION\"," +
            "tier = TIER, hallmark = HALLMARK, chromosome_band = \"CHRBAND\", somatic = SOMATIC, " +
            "germline = GERMLINE, cancer_syndrome = \"CANCERSYN\", molecular_genetics = \"MOLGEN\", " +
            "other_germline_mut = \"OTHERMUT\"  RETURN cg.gene_symbol"

    fun processCosmicGeneNode(cosmicGene: CosmicGeneCensus) {
        loadCosmicGeneNode(cosmicGene)
        loadTumorList(cosmicGene.geneSymbol, cosmicGene.somaticTumorTypeList,"Somatic")
        loadTumorList(cosmicGene.geneSymbol, cosmicGene.germlineTumorTypeList,"Germline")
    }

   private fun loadCosmicGeneNode(cosmicGene: CosmicGeneCensus): String {
        if (!cancerGeneLoaded(cosmicGene.geneSymbol)) {
            val merge = cypherLoadTemplate.replace("GENESYMBOL", cosmicGene.geneSymbol)
                .replace("GENENAME", cosmicGene.geneName)
                .replace("ENTREZ", cosmicGene.entrezGeneId)
                .replace("LOCATION", cosmicGene.genomeLocation)
                .replace("TIER", cosmicGene.tier.toString())
                .replace("HALLMARK", cosmicGene.hallmark.toString())
                .replace("CHRBAND", cosmicGene.chromosomeBand)
                .replace("SOMATIC", cosmicGene.somatic.toString())
                .replace("GERMLINE", cosmicGene.germline.toString())
                .replace("CANCERSYN", cosmicGene.cancerSyndrome)
                .replace("MOLGEN", cosmicGene.molecularGenetics)
                .replace("OTHERMUT", cosmicGene.otherGermlineMut)
            return Neo4jConnectionService.executeCypherCommand(merge)
        }
        return "CosmicGene: ${cosmicGene.cancerSyndrome} has already been loaded"
    }

    private fun loadTumorList(geneSymbol: String, tumorList:List<String>, tumorType: String) {
       tumorList.map { tt -> TumorTypeService.resolveTumorType(tt) }
            .filter { tt -> tt.isNotEmpty() }
            .forEach { tt ->
                run {
                    Neo4jConnectionService.executeCypherCommand(
                        "MERGE (ca:CosmicAnnotation{annotation_value: \"$tt\"})"
                    )
                    // add TumorType label if novel
                    val labelExistsQuery = "MERGE (ca:CosmicAnnotation{annotation_value:\"$tt\"}" +
                            "RETURN apoc.label.exists(ca, \"TumorType\") AS output;"
                    if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery).uppercase() == "FALSE") {
                        Neo4jConnectionService.executeCypherCommand(
                            "MATCH (ca:CosmicAnnotation{annotation_value:\"$tt\"} " +
                                    "CALL apoc.create.addLabels(ca,[\"TumorType\"] YIELD node RETURN node"
                        )
                    }
                    // create CosmicGene -> CosmicAnnotation
                    Neo4jConnectionService.executeCypherCommand(
                        "MATCH (cg:CosmicGene), (ca:CosmicAnnotation) WHERE cg.gene_symbol = \"$geneSymbol\" " +
                                " AND ca.annotation_vale = \"$tt\" MERGE (cg) -" +
                                "[r: HAS_TUMOR_TYPE {type: \"$tumorType\"}] ->(ca) "
                    )
                }
            }
    }

    /*
    Function to determine if a CancerGene node for a specified gene
    has already been loaded
     */
    fun cancerGeneLoaded(geneSymbol: String): Boolean =
        Neo4jUtils.nodeLoadedPredicate(
            "OPTIONAL MATCH (cg:CosmicGene{gene_symbol: $geneSymbol }) " +
                    " RETURN cg IS NOT NULL AS PREDICATE"
        )


}