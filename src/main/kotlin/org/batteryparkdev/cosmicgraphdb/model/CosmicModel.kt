package org.batteryparkdev.cosmicgraphdb.model

import org.batteryparkdev.nodeidentifier.model.NodeIdentifier
import org.batteryparkdev.placeholder.loader.PubMedPlaceholderNodeLoader

interface CosmicModel {
    abstract fun getNodeIdentifier(): NodeIdentifier
    /*
    Generic function to create a HAS_PUBLICATION relationship between a node and a
    Publication/PubMed node
     */
    fun createPubMedRelationship( pubId:Int) {
        if (pubId > 0) {
            val nodeId = getNodeIdentifier()
            val pub = PubMedPlaceholderNodeLoader(pubId.toString(), nodeId.idValue,
                nodeId.primaryLabel, nodeId.idProperty
            )
            pub.registerPubMedPublication()
        }
    }
}