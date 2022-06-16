package org.batteryparkdev.cosmicgraphdb.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import org.apache.commons.csv.CSVRecord
import org.batteryparkdev.io.CSVRecordSupplier
import java.nio.file.Paths
import kotlin.streams.asSequence

abstract class TestCosmicModel {
    /*
Apache Commons CSV parser
*/
    @OptIn(ExperimentalCoroutinesApi::class)
     fun CoroutineScope.produceCSVRecords(filename: String) =
        produce<CSVRecord> {
            val path = Paths.get(filename)
            CSVRecordSupplier(path).get().asSequence()
                .filter { it.size() > 1 }
                .forEach {
                    send(it)
                    delay(20)
                }
        }
}