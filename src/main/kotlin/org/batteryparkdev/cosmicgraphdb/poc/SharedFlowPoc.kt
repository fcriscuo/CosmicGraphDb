package org.batteryparkdev.cosmicgraphdb.poc

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicMutation
import org.batteryparkdev.cosmicgraphdb.cosmic.model.CosmicTumor
import org.batteryparkdev.cosmicgraphdb.io.TsvRecordSequenceSupplier
import java.nio.file.Paths


class CsvRecordFlow (private val externalScope:CoroutineScope,
                     filename: String,
    private val intervalMs:Long = 500){
    // Backing property to avoid flow emissions from other classes
    private val csvFlow = MutableSharedFlow<CSVRecord>(replay = 0)
    val csvRecordFlow: SharedFlow<CSVRecord> = csvFlow
    val path = Paths.get(filename)
    init {
        externalScope.launch {
            TsvRecordSequenceSupplier(path).get()
                .forEach {
                    println(" record ${it.get(0)}")
                    csvFlow.emit(it)
                    delay(intervalMs)
                }
        }
    }
}

class TumorReceiver (
    private val tumorFlow: CsvRecordFlow,
    private val externalScope: CoroutineScope
        ){
    init {
        externalScope.launch {
            tumorFlow.csvRecordFlow.collect { parseTumorRecord(it) }
            delay(200)
        }
    }
    suspend fun parseTumorRecord(record: CSVRecord){
        val cosmicTumor =  CosmicTumor.parseCsvRecord(record)
        println("CosmicTumor  ${cosmicTumor.tumorId}")
    }
}

class MutationReceiver (
    private val mutationFlow: CsvRecordFlow,
    private val externalScope: CoroutineScope
){
    init {
        externalScope.launch {
            mutationFlow.csvRecordFlow.collect { parseMutationRecord(it) }
            delay(200)
        }
    }
    suspend fun parseMutationRecord(record: CSVRecord){
        val cosmicMutation =  CosmicMutation.parseCsvRecord(record)
        println("CosmicMutation  ${cosmicMutation.mutationId}")
    }
}

fun main(args: Array<String>) = runBlocking {
    val scope = CoroutineScope(Dispatchers.IO)
    val filename = "./data/sample_CosmicMutantExportCensus.tsv"
    val recordFlow = CsvRecordFlow(scope, filename)
    val tr = TumorReceiver(recordFlow, scope)
    val mr = MutationReceiver(recordFlow, scope)
    delay(300_000L)
}
