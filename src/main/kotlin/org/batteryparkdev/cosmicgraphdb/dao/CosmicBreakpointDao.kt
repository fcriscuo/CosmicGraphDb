package org.batteryparkdev.cosmicgraphdb.dao

import org.batteryparkdev.cosmicgraphdb.model.CosmicBreakpoint
import org.batteryparkdev.neo4j.service.Neo4jConnectionService

/*
DAO responsible for loading a CosmicBreakpoint entry into the Neo4j database
as a CosmicBreakpoint node and related CosmicType nodes for site, histology
and mutation types
 */

   fun loadCosmicBreakpoint( breakpoint: CosmicBreakpoint): Int {
      val cypher = breakpoint.generateMergeCypher()
           .plus(breakpoint.site.generateParentRelationshipCypher(breakpoint.nodeName))
           .plus(breakpoint.histology.generateParentRelationshipCypher(breakpoint.nodeName))
           .plus(breakpoint.mutationType.generateParentRelationshipCypher(breakpoint.nodeName))
          .plus(" RETURN ${breakpoint.nodeName}")
       println("Breakpoint cypher: $cypher")
       val node = Neo4jConnectionService.executeCypherCommand(cypher)
       return breakpoint.mutationId
   }

