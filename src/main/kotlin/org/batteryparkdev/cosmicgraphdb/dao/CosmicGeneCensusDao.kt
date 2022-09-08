package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicAnnotationFunctions
import org.batteryparkdev.cosmicgraphdb.model.CosmicGeneCensus
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.hgnc.HgncDao
import org.batteryparkdev.genomicgraphcore.hgnc.HgncModel
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifier
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition
import org.batteryparkdev.genomicgraphcore.neo4j.service.Neo4jUtils

class CosmicGeneCensusDao(private val cosmicGene: CosmicGeneCensus) {

    fun generateLoadCosmicModelCypher(): String =
        generateMergeCypher().plus(generateGeneMutationCollectionNodeCypher())
            .plus(generateGeneAnnotationCollectionCypher())
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    cosmicGene.somaticTumorTypeList,
                    "SomaticTumorType", CosmicGeneCensus.annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    cosmicGene.germlineTumorTypeList, "GermlineTumorType",
                    CosmicGeneCensus.annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    cosmicGene.tissueTypeList,
                    "TissueType", CosmicGeneCensus.annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    cosmicGene.roleInCancerList,
                    "RoleInCancer", CosmicGeneCensus.annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    cosmicGene.mutationTypeList, "MutationType",
                    CosmicGeneCensus.annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    cosmicGene.otherSyndromeList, "OtherSyndrome",
                    CosmicGeneCensus.annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateAnnotationCypher(
                    cosmicGene.synonymList, "Synonym",
                    CosmicGeneCensus.annoCollNodename
                )
            )
            .plus(
                CosmicAnnotationFunctions.generateTranslocationCypher(
                    cosmicGene.geneSymbol,
                    cosmicGene.translocationPartnerList
                )
            )
            .plus(" RETURN ${CosmicGeneCensus.nodename}")

    private fun generateGeneMutationCollectionNodeCypher(): String =
        "CALL apoc.merge.node([\"GeneMutationCollection\"], " +
                " {gene_symbol: ${cosmicGene.geneSymbol.formatNeo4jPropertyValue()}," +
                " {created: datetime(.formatNeo4jPropertyValue(),{}) YIELD node as ${CosmicGeneCensus.mutCollNodename} \n " +
                " CALL apoc.merge.relationship (${CosmicGeneCensus.nodename}, \"HAS_MUTATION_COLLECTION\", " +
                "  {},{created: datetime(.formatNeo4jPropertyValue(), ${CosmicGeneCensus.mutCollNodename},{} ) YIELD rel AS mut_rel \n "

    private fun generateGeneAnnotationCollectionCypher(): String =
        "CALL apoc.merge.node([\"GeneAnnotationCollection\"], " +
                " {gene_symbol: ${cosmicGene.geneSymbol.formatNeo4jPropertyValue()}," +
                " {created: datetime(.formatNeo4jPropertyValue(),{}) YIELD node as ${CosmicGeneCensus.annoCollNodename} \n " +
                " CALL apoc.merge.relationship (${CosmicGeneCensus.nodename}, \"HAS_ANNOTATION_COLLECTION\", " +
                "  {},{created: datetime(.formatNeo4jPropertyValue(), ${CosmicGeneCensus.annoCollNodename},{} ) YIELD rel AS anno_rel \n "


    private fun generateMergeCypher(): String =
        when (Neo4jUtils.nodeExistsPredicate(cosmicGene.getNodeIdentifier())) {
            //update an existing CosmicGene node (i.e. placeholder)
            true -> "CALL apoc.merge.node( [\"CosmicGene\",\"CensusGene\",\"\"Gene\"," +
                    "{  gene_symbol: ${cosmicGene.geneSymbol.formatNeo4jPropertyValue()}, " +
                    "{}," +
                    " {gene_name: ${cosmicGene.geneName.formatNeo4jPropertyValue()}," +
                    " entrez_gene_id: ${cosmicGene.entrezGeneId.formatNeo4jPropertyValue()}," +
                    " genome_location: ${cosmicGene.genomeLocation.formatNeo4jPropertyValue()}," +
                    " tier: ${cosmicGene.tier}, " +
                    " hallmark: ${cosmicGene.hallmark}, " +
                    " chromosome_band: ${cosmicGene.chromosomeBand}, " +
                    " somatic: ${cosmicGene.somatic}, germline: ${cosmicGene.germline}, " +
                    " cancer_syndrome: ${cosmicGene.cancerSyndrome.formatNeo4jPropertyValue()}," +
                    " molecular_genetics: ${cosmicGene.molecularGenetics.formatNeo4jPropertyValue()}, " +
                    " other_germline_mut: ${cosmicGene.otherGermlineMut.formatNeo4jPropertyValue()}, " +
                    " cosmic_id: ${cosmicGene.cosmicId.formatNeo4jPropertyValue()} , " +
                    " cosmic_gene_name: ${cosmicGene.cosmicGeneName.formatNeo4jPropertyValue()} , " +
                    "  created: datetime(.formatNeo4jPropertyValue()) YIELD node as ${CosmicGeneCensus.nodename} \n"
            // create a new CosmicGene new node
            false -> "CALL apoc.merge.node( [\"CosmicGene\",\"CensusGene\",\"\"Gene\"]," +
                    "{  gene_symbol: ${cosmicGene.geneSymbol.formatNeo4jPropertyValue()}," +
                    " {gene_name: ${cosmicGene.geneName.formatNeo4jPropertyValue()}," +
                    " entrez_gene_id: ${cosmicGene.entrezGeneId.formatNeo4jPropertyValue()}," +
                    " genome_location: ${cosmicGene.genomeLocation.formatNeo4jPropertyValue()}," +
                    " tier: ${cosmicGene.tier}, " +
                    " hallmark: ${cosmicGene.hallmark}, " +
                    " chromosome_band: ${cosmicGene.chromosomeBand}, " +
                    " somatic: ${cosmicGene.somatic}, germline: ${cosmicGene.germline}, " +
                    " cancer_syndrome: ${cosmicGene.cancerSyndrome.formatNeo4jPropertyValue()}," +
                    " molecular_genetics: ${cosmicGene.molecularGenetics.formatNeo4jPropertyValue()}, " +
                    " other_germline_mut: ${cosmicGene.otherGermlineMut.formatNeo4jPropertyValue()}," +
                    " cosmic_id: ${cosmicGene.cosmicId.formatNeo4jPropertyValue()}, " +
                    "cosmic_gene_name: ${cosmicGene.cosmicGeneName.formatNeo4jPropertyValue()}, " +
                    "  created: datetime(.formatNeo4jPropertyValue(),{}) YIELD node as ${CosmicGeneCensus.nodename} \n"
        }

    companion object : CoreModelDao {
        override val modelRelationshipFunctions: (CoreModel) -> Unit = ::completeMutationRelationships

        fun completeMutationRelationships(model: CoreModel) {
            if (model is CosmicGeneCensus) {
                completeRelationshipToEntrez(model)
            }
            completeRelationshipToHgnc(model)
        }

        /*
      Create a relationship to a Hgnc node loaded into the database as part of the GenomicGraphCore
       */
        private fun completeRelationshipToHgnc(model: CoreModel) {
            if (model is HgncModel){
                HgncDao.registerChildRelationshipToHgnc(model.hgncId, model)
            }
        }

        /*
     Create a relationship to an Entrez node loaded into the database as part of the GenomicGraphCore
      */
        private fun completeRelationshipToEntrez(cosmicGene: CosmicGeneCensus) {
            if (cosmicGene.entrezGeneId.toInt() > 0) {
                val entrez = NodeIdentifier("Entrez", "entrez_id", cosmicGene.entrezGeneId)
                NodeIdentifierDao.defineRelationship(
                    RelationshipDefinition(cosmicGene.getNodeIdentifier(), entrez, "HAS_ENTREZ")
                )
            }
        }
    }
}