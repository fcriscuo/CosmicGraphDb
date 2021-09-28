package org.batteryparkdev.cosmicgraphdb.property

object DatafilePropertiesService
    : AbstractPropertiesService() {
        private const val PROPERTIES_FILE = "/datafiles.properties"

        init {
            resolveFrameworkProperties(PROPERTIES_FILE)
        }
}

fun main() {
    val dir = DatafilePropertiesService.resolvePropertyAsString("cosmic.data.directory")
    val filename = dir + DatafilePropertiesService.resolvePropertyAsString("file.cosmic.gene")
    println(filename)
}