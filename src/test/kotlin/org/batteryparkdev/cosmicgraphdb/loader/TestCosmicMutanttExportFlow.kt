package org.batteryparkdev.cosmicgraphdb.loader

import com.google.common.base.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import org.batteryparkdev.property.service.ConfigurationPropertiesService
import java.util.concurrent.TimeUnit

class TestCosmicMutantExportFlow(val filename:String) {
    val scope = CoroutineScope(Dispatchers.IO)
    val stopwatch = Stopwatch.createStarted()
    val valueFlow = CosmicMutantExportFlow(scope, filename)
    val tr = TumorReceiver(valueFlow, scope)
    val mr = MutationReceiver(valueFlow, scope)
    fun runTest() {
        stopwatch.elapsed(TimeUnit.SECONDS)
        println("Elapsed time: ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
        println("Cancelling children")
        with(coroutineContext) {
            stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)
            println("Elapsed time: ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds")
            println("Cancelling children")
            cancelChildren()
        }
    }
}
/*
main function for Neo4j integration testing
 */
fun main(args: Array<String>) = runBlocking {
    val filename = ConfigurationPropertiesService.resolveCosmicSampleFileLocation("CosmicMutantExportCensus.tsv")
   TestCosmicMutantExportFlow(filename).runTest()

}