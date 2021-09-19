package org.batteryparkdev.cosmicgraphdb.cosmic.model

data class CosmicType (val label: String,
                       val primary: String,
                       val subtype1: String = "NS",
                       val subtype2: String = "NS",
                       val subtype3: String = "NS") {
}