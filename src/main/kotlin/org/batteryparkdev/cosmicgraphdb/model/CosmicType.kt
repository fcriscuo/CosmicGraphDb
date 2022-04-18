package org.batteryparkdev.cosmicgraphdb.model

import java.util.*

data class CosmicType(
    val label: String,
    val primary: String,
    val subtype1: String = "NS",
    val subtype2: String = "NS",
    val subtype3: String = "NS"
) {

    fun generateCosmicTypeCypher(parentNodeName: String) =
        generateMergeCypher()
            .plus(generateParentRelationshipCypher(parentNodeName))

    private fun generateMergeCypher(): String =
        " CALL apoc.merge.node([\"CosmicType\",\"${label}\"], " +
                "{ cosmic_type_id: ${UUID.randomUUID().hashCode()}}, " +
                "{ primary: \"${primary}\", " +
                "  subtype1: \"${subtype1}\", " +
                "  subtype2: \"${subtype2}\", " +
                "  subtype3: \"${subtype3}\", " +
                " created: datetime() }," +
                "{ last_mod: datetime()}) YIELD node AS ${label.lowercase()}\n "

    private fun generateParentRelationshipCypher(parentNodeName: String): String {
        val relationship = "HAS_".plus(label.uppercase())
        val relName = "rel_".plus(label.lowercase())
        return generateMergeCypher().plus(
            " CALL apoc.merge.relationship($parentNodeName, '$relationship' ," +
                    " {}, {created: datetime()}," +
                    " ${label.lowercase()}, {}) YIELD rel AS $relName\n"
        )
    }
}