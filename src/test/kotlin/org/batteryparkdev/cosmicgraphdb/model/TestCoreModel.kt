package org.batteryparkdev.cosmicgraphdb.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.genomicgraphcore.common.CoreModel
import org.batteryparkdev.genomicgraphcore.common.CoreModelCreator
import org.batteryparkdev.genomicgraphcore.common.io.CSVRecordSupplier
import java.nio.file.Paths
import kotlin.streams.asSequence

class TestCoreModel (val creator: CoreModelCreator)  {
    var nodeCount = 0
    private val LIMIT = 4000L

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get().limit(LIMIT).asSequence()
                .filter { it.size() > 1 }
                .forEach {
                    send(it)
                    delay(20)
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.generateModels(records: ReceiveChannel<CSVRecord>) =
        produce<CoreModel> {
            for (record in records){
                val model = creator.createCoreModelFunction(record)
                if (model.isValid()) {
                    send(model)
                }
                delay(20L)
            }
        }

    fun loadModels(filename: String) = runBlocking {
        val models = generateModels(produceCSVRecords(filename))
        for (model in models) {
            nodeCount += 1
            if (nodeCount % 50 == 0 ) {
                println(model.generateLoadModelCypher())
            }
        }
        println("Loaded record count for ${creator::class.java.name } = $nodeCount")
    }
}