#Load the Cypher constraints for the COSMIC nodes
result1 = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/Cosmic_Constraints.cql`, String)

#CosmicActionability
result2 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_actionability.cql`, String)
actionCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicActionability) return count(n) '`, String)
printf("%s CosmicActionability nodes loaded ", actionCount)
println(result2)

#CosmicBreakpoint
result3 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_breakpoint.cql`, String)
breakpointCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicBreakpointMutation) return count(n) '`, String)
printf("%s CosmicBreakpointMutation nodes loaded %s", breakpointCount)
println(result3)

#CosmicClassification
result4 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_classification.cql`, String)
classificationCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicClassification) return count(n) '`, String)
printf("%s CosmicClassification nodes loaded %s", classificationCount)
println(result4)

#CosmicCMC
result5 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_cmc.cql`, String)
cmcCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicCMC) return count(n) '`, String)
printf("%s CosmicCMC nodes loaded %s", cmcCount)
println(result5)

#CosmicCompleteCNA
result6 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_complete_cna.cql`, String)
cnaCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicCNA) return count(n) '`, String)
printf("%s CosmicCNA nodes loaded %s", cnaCount)
println(result6)

#CosmicDiffMethylation
result7 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_diff_meth.cql`, String)
diffCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicDiffMethylation) return count(n) '`, String)
printf("%s CosmicGene nodes loaded %s", geneCount)
println(result7)

#CosmicGene
result8 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_gene_census.cql`, String)
geneCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicGene) return count(n) '`, String)
printf("%s CosmicDiffMethylation nodes loaded %s", geneCount)
println(result8)

#CosmicGeneExpression
result9 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_gene_expression.cql`, String)
expCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicGeneExpression) return count(n) '`, String)
printf("%s CosmicGeneExpression nodes loaded %s", expCount)
println(result9)

#CosmicHallmark
result10=  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_hallmark.cql`, String)
hallmarkCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicHallmark) return count(n) '`, String)
printf("%s CosmicHallmark nodes loaded %s", hallmarkCount)
println(result10)

#CosmicMutation
result11 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_mutant_export.cql`, String)
mutationCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicMutation) return count(n) '`, String)
printf("%s CosmicMutation nodes loaded %s", mutationCount)
println(result11)

#CosmicMutationTracking
result12 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_mutation_tracking`, String)
trackCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicMutationTracking) return count(n) '`, String)
printf("%s CosmicMutationTracking nodes loaded %s", trackCount)
println(result12)

#CosmicNCV
result13 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_ncv.cql`, String)
ncvCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicNCV) return count(n) '`, String)
printf("%s CosmicNCV nodes loaded %s", ncvCount)
println(result13)

#CosmicResistanceMutation
result14 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_resistance_mutations.cql`, String)
resistCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicResistanceMutation) return count(n) '`, String)
printf("%s CosmicResistanceMutation nodes loaded %s", resistCount)
println(result14)

#CosmicSample
result15 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_sample.cql`, String)
sampleCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicSample) return count(n) '`, String)
printf(" %s CosmicSample nodes loaded", sampleCount)
println(result15)

#CosmicStructMutation
result16 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_struct_export.cql`, String)
structCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:CosmicStructuralMutation) return count(n) '`, String)
printf(" %s CosmicStructuralMutation nodes loaded", structCount)
println(result16)

#Complete relationships
result17 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/Cosmic_Relationships.cql`, String)
println(result17)

#Publication
result18 =  read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j -f ./softwaredev/CosmicGraphDb/cql/cosmic_pubmed_ids.cql`, String)
pubCount = result = read(`cypher-shell -a neo4j://tosca.local:7687 --format plain -u neo4j -p fjc92677 -d neo4j 'match (n:Publication) return count(n) '`, String)
printf("%s Publication nodesloaded ", pubCount)
println(result18)