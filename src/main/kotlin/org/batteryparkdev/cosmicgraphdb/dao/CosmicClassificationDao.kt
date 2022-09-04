package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicClassification
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.NodeIdentifierDao
import org.batteryparkdev.genomicgraphcore.neo4j.nodeidentifier.RelationshipDefinition

class CosmicClassificationDao(private val cosmicClassification: CosmicClassification) {

    fun generateCosmicClassificationCypher(): String = "CALL apoc.merge.node([\"CosmicClassification\"]," +
            "{ phenotype_id: ${cosmicClassification.cosmicPhenotypeId.formatNeo4jPropertyValue()}}," +
            " { nci_code: ${cosmicClassification.nciCode.formatNeo4jPropertyValue()}," +
            " efo_url: ${cosmicClassification.efoUrl.formatNeo4jPropertyValue()}," +
            " created: datetime() }," +
            "{ last_mod: datetime()}) YIELD node AS ${CosmicClassification.nodename} \n "
                .plus(cosmicClassification.siteType.generateCosmicTypeCypher(CosmicClassification.nodename))
                .plus(cosmicClassification.histologyType.generateCosmicTypeCypher(CosmicClassification.nodename))
                .plus(" RETURN ${CosmicClassification.nodename}\n")

    companion object : CoreModelDao {
        final val nodename = "classification"

        override val modelRelationshipFunctions: (CoreModel) -> Unit =
            ::completeClassificationRelationships

        fun completeClassificationRelationships(model: CoreModel) {
            if (model is CosmicClassification) {
                completeSiteTypeRelationship(model)
                completeHistologyTypeRelationship(model)
            }
        }

        private fun completeSiteTypeRelationship(classification: CosmicClassification) {
            NodeIdentifierDao.defineRelationship(RelationshipDefinition(
                classification.getNodeIdentifier(), classification.cosmicSiteType.getNodeIdentifier(),
                "HAS_SITE_TYPE"
            ))
        }

        private fun completeHistologyTypeRelationship(classification: CosmicClassification) {
            NodeIdentifierDao.defineRelationship(RelationshipDefinition(
                classification.getNodeIdentifier(), classification.histologyType.getNodeIdentifier(),
                "HAS_HISTOLOGY_TYPE"
            ))
        }
    }
}