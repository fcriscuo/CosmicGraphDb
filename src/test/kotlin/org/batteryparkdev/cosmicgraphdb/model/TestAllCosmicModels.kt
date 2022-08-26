package org.batteryparkdev.cosmicgraphdb.model

class TestAllCosmicModels {
}
/*
Class that will invoke an integration test for every Cosmic model class
These tests verify that the CSV/TSV column mappings are correct.
Cosmic file columns can change slightly from one release to another so these integration
tests can identify which model classes need to be updated for a new COSMIC release.
They are not destructive to an existing Neo4j database.
Each model integration test can also be run on an individual basis.
 */
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