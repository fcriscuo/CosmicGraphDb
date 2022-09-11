package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicType
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

class CosmicTypeDao (private val type: CosmicType) {

    fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
        .plus(" RETURN  ${CosmicType.nodename}")

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"CosmicType\",\"${type.label}\"], " +
                "{ cosmic_type_id: $type.typeId}, " +
                "{ primary: ${type.primary.formatNeo4jPropertyValue()}\", " +
                "  subtype1: ${type.subtype1.formatNeo4jPropertyValue()}\", " +
                "  subtype2: ${type.subtype2.formatNeo4jPropertyValue()}\", " +
                "  subtype3: ${type.subtype3.formatNeo4jPropertyValue()}\", " +
                " created: datetime() }," +
                "{ last_mod: datetime()}) YIELD node AS ${type.label.lowercase()}\n "

    companion object: CoreModelDao {

        private fun nullFunction(model: CoreModel) {}
        // relationships are handled in the CosmicClassificationDao class
        override val modelRelationshipFunctions: (CoreModel) -> Unit
            = ::nullFunction
    }
}