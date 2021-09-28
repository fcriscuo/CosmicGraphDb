package org.batteryparkdev.cosmicgraphdb.property

object DatafilePropertiesService
    : AbstractPropertiesService() {
        private const val PROPERTIES_FILE = "/atafiles.properties"

        init {
            ApplicationPropertiesService.resolveFrameworkProperties(PROPERTIES_FILE)
        }
}