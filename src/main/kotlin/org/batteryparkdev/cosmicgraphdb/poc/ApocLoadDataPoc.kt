package org.batteryparkdev.cosmicgraphdb.poc

import org.batteryparkdev.cosmicgraphdb.neo4j.Neo4jConnectionService
import org.batteryparkdev.cosmicgraphdb.property.DatafilePropertiesService

/*
POC to evaluate using Neo4j APOC commands to read a delimited file
and create database nodes.
The CosmicBreakpointsExport.tsv file will be used as an example
Lessons learned: for tab-delimited files use sep='TAB'
                 in conf file must specify apoc.import.file.use_neo4j_config=false
                 (this is documented as the default setting, but it isn't)
                  must also specify apoc.import.file.enabled=true
                  column names with spaces must be enclosed with tics (`)
 */

private val commitStatement = "USING PERIODIC COMMIT"


fun loadTsvFile(filename: String) {
val loadStatement = "CALL apoc.load.csv('$filename', {sep:'TAB', limit:20, " +
        "mapping:{" +
        " `Sample name`: {name:'sample_name'}," +
        " ID_SAMPLE: {type:'int', name:'sample_id'}, " +
        " ID_TUMOUR: {type:'int', name:'tumor_id'}," +
        " `Primary site`: {name:'primary_site'}, " +
        " `Site subtype 1`: {name:'site_subtype_1'}, " +
        " `Site subtype 2`: {name:'site_subtype_2'}, " +
        " `Site subtype 3`: {name:'site_subtype_3'}, " +
        " `Primary histology`: {name:'primary_histology'}, " +
        " `Histology subtype 1`: {name:'histology_subtype_1'}, " +
        " `Histology subtype 2`: {name:'histology_subtype_2'}, " +
        " `Histology subtype 3`: {name:'histology_subtype_3'}, " +
        " `MUTATION Type`: { name:'mutation_type'}, " +
        " `Mutation ID`: {type:'int', name:'mutation_id'}," +
        " `Breakpoint Order`: {type:'int', name:'breakpoint_order'}, " +
        " GRCh: {type:'int', name:'grch'}, " +
        " `Chrom  From`: { name:'chrom_from'}, " +
        " `Location From min`: {type:'int', name:'location_from_min'}, " +
        " `Location From max`: {type:'int', name:'location_from_max'}, " +
        " `Strand  From`: { name:'strand_from'}, " +
        " `Chrom  To`: { name:'chrom_to'}, " +
        " `Location To min`: {type:'int', name:'location_to_min'}, " +
        " `Location To max`: {type:'int', name:'location_to_max'}, " +
        " `Strand  To`: { name:'strand_from'}, " +
        " `Pubmed_PMID`: {type:'int', name:'pubmed_id'}, " +
        " `ID Study`: {type:'int', name:'study_id'} " +
        " }}) " +
        "YIELD map AS row " +
        " CALL apoc.merge.node(['PocBreakpoint'], {sample_name:row.sample_name, sample_id:row.sample_id, " +
       " tumor_id:row.tumor_id, primary_site:row.primary_site, mutation_id:row.mutation_id})  YIELD node as node1 " +
        " CALL apoc.merge.node(['CosmicType','HistologyPOC'], {primary:row.primary_histology, subtype1:row.histology_subtype_1, " +
        " subtype2:row.histology_subtype_2, subtype3:row.histology_subtype_3}) YIELD node as node2 " +
        " CALL apoc.create.relationship(node1,'HAS_HISTOLOGY',{props:[]},node2) YIELD rel " +
        " return count(*) "
    val count = Neo4jConnectionService.executeCypherCommand(loadStatement)
    println("Row count = $count")

}

fun main() {
    val dataDirectory =  DatafilePropertiesService.resolvePropertyAsString("cosmic.sample.data.directory")
    val cosmicBreakpointsFileName = dataDirectory +
            DatafilePropertiesService.resolvePropertyAsString("file.cosmic.breakpoints.export")
    loadTsvFile(cosmicBreakpointsFileName)
}