package org.batteryparkdev.cosmicgraphdb.model

class TestAllCosmicModels {
}
fun main() {
    TestCosmicBreakpoint().testCosmicModel()
    TestCosmicClassification().testCosmicModel()
    TestCosmicCodingMutation().testCosmicModel()
    TestCosmicCompleteCNA().testCosmicModel()
    TestCosmicCompleteExpression().testCosmicModel()
    TestCosmicDiffMethylation().testCosmicModel()
    TestCosmicFusion().testCosmicModel()
    TestCosmicGeneCensus().testCosmicModel()
    TestCosmicHallmark().testCosmicModel()
    TestCosmicHGNC().testCosmicModel()
    TestCosmicNCV().testCosmicModel()
    TestCosmicPatient().processCSVRecords()
    TestCosmicResistanceMutation().testCosmicModel()
    TestCosmicSample().testCosmicModel()
    TestCosmicStruct().testCosmicModel()
    println("FINIS......")
}