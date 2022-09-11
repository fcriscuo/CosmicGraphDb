package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicNCV
import org.batteryparkdev.cosmicgraphdb.model.CosmicType
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelDao
import org.batteryparkdev.genomicgraphcore.common.formatNeo4jPropertyValue

class CosmicTypeDao (private val type: CosmicType) {

    fun generateLoadCosmicModelCypher(): String = generateMergeCypher()
        .plus(" RETURN  ${CosmicType.nodename}")


    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"CosmicType\",\"${label}\"], " +
                "{ cosmic_type_id: $typeId}, " +
                "{ primary: ${primary.formatNeo4jPropertyValue()}\", " +
                "  subtype1: ${subtype1.formatNeo4jPropertyValue()}\", " +
                "  subtype2: ${subtype2.formatNeo4jPropertyValue()}\", " +
                "  subtype3: ${subtype3.formatNeo4jPropertyValue()}\", " +
                " created: datetime() }," +
                "{ last_mod: datetime()}) YIELD node AS ${label.lowercase()}\n "

    private fun generateParentRelationshipCypher(parentNodeName: String): String {
        val relationship = "HAS_".plus(label.uppercase()).plus("_TYPE")
        val relName = "rel_".plus(label.lowercase())
        return " CALL apoc.merge.relationship($parentNodeName, '$relationship' ," +
                " {}, {created: datetime()}," +
                " ${label.lowercase()}, {}) YIELD rel AS $relName\n"
    }

    companion object: CoreModelDao {
        override val modelRelationshipFunctions: (CoreModel) -> Unit
            get() = TODO("Not yet implemented")
    }
}