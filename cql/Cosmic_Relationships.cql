
// CosmicSampleClassification -> CosmicActionability
MATCH (act:CosmicActionability) WHERE act.classification_id IS NOT NULL
MATCH (clas:CosmicSampleClassification) WHERE class.phenotype_id = act.classification_id
MERGE (class)-[r:HAS_AVAILABLE_ACTIONS]->(act)
;

// SampleMutationCollection -> CosmicBreakpoint
MATCH (break:CosmicBreakpoint) WHERE break.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = break.sample_id
MERGE (smc)-[r1:HAS_BREAKPOINT_MUTATION]->(break)
;

// GeneMutationCollection -> CosmicCMC
MATCH (cmc:CosmicCMC) WHERE cmc.gene_symbol IS NOT NULL
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = cmc.gene_symbol
MERGE (gmc) -[r1:HAS_CODING_MUTATION]->(cmc)
;

// SampleMutationCollection -> CosmicCNA
MATCH (cna:CosmicCNA) WHERE cna.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = cna.sample_id
MERGE (smc)-[r1:HAS_CNA_MUTATION]->(cna)
;

// GeneMutationCollection -> CosmicCNA
MATCH (cna:CosmicCNA) WHERE cna.gene_symbol IS NOT NULL
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = cna.gene_symbol
MERGE (gmc)-[r2:HAS_CNA_MUTATION]->(cna)
;

// SampleMutationCollection -> CosmicDiffMethylation
MATCH (meth:CosmicDiffMethylation) WHERE meth.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = meth.sample_id
MERGE (smc) -[r1:HAS_DIFFERENTIAL_METHYLATION] ->(meth)
;

// GeneMutationCollection -> CosmicDiffMethylation
MATCH (meth:CosmicDiffMethylation) WHERE meth.gene_symbol IS NOT NULL
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = meth.gene_symbol
MERGE (gmc)-[r2:HAS_DIFFERENTIAL_METHYLATION] ->(meth)
;

// CosmicGene -> CosmicGene (translocation partners)
MATCH (gene:CosmicGene) WHERE  gene.translocation_partners IS NOT NULL
UNWIND gene.translocation_partners AS translocation
MATCH(partner:CosmicGene) WHERE partner.gene_symbol=translocation
MERGE (gene)-[r:HAS_TRANSLOCATION_PARTNER]-(partner)
;

// CosmicGene -> EntrezGene
// Add URL to Entrez Gene page if Entrez ID is available
MATCH (cg:CosmicGene) WHERE cg.entrez_id is not null
SET cg.entrez_gene_url= replace("https://www.ncbi.nlm.nih.gov/gene/?term=XXXX","XXXX",cg.entrez_id)
MATCH (eg:EntrezGene) WHERE eg.gene_id = cg.entrez_id
MERGE (cg)-[r1:HAS_ENTREZ_GENE]-(eg)
;

//  CosmicGeneHallmarkCollection -> CosmivHallmark
MATCH (ch: CosmicHallmark) WHERE ch.gene_symbol IS NOT NULL
MATCH (cghc:CosmicGeneHallmarkCollection) where cghc.gene_symbol = ch.gene_symbol
MERGE (cghc) -[HAS_COSMIC_HALLMARK]->(ch)
;

// CosmicGene -> UniProtEntry
MATCH (cg:CosmicGene)
MATCH (uni:UniProtEntry) WHERE cg.gene_symbol = uni.gene_symbol
MERGE (cg)-[r1:HAS_UNIPROT_ENTRY]->(uni)
;

// CosmicGene -> HGNC
CALL apoc.periodic.iterate('
CALL apoc.load.csv("/Volumes/Sea%TBExt/COSMIC_rel97/CosmicHGNC.tsv",
{ header:true,  sep:"TAB"}
)
YIELD nodeRecordNo, map AS nodeRecord RETURN nodeRecord, nodeRecordNo
','
WHERE NOT nodeRecord.COSMIC_GENE_NAME IS NULL
MATCH (cg:CosmicGene) where cg.gene_symbol = nodeRecord.COSMIC_GENE_NAME
SET cg.hgnc_id = apoc.text.join(["HGNC",nodeRecord.HGNC_ID],":")
WITH cg
MATCH (h:Hgnc) WHERE h.hgnc_id = cg.hgnc_id
MERGE (cg)-[r1:HAS_HGNC]->(h)
',
{batchSize:4000, iterateList:true, parallel:true});

// SampleMutationCollection -> CosmicGeneExpression
// GeneMutationCollection -> CosmicGeneExpression
MATCH (express:CosmicGeneExpression) WHERE express.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = express.sample_id
MERGE (smc) -[r1:HAS_GENE_EXPRESSION_VARIANT] ->(express)
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = express.gene_symbol
MERGE (gmc) -[r2:HAS_GENE_EXPRESSION_VARIANT] ->(express)
;

// SampleMutationCollection -> CosmicMutation
// GeneMutationCollection -> Cosmic Mutation
MATCH (ccm:CosmicMutation)
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = ccm.sample_id
MERGE (smc)-[r1:HAS_MUTATION]->(ccm)
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = ccm.gene_symbol
MERGE (gmc)-[r2:HAS_MUTATION]->(ccm)
;

//  CosmicMutation -> CosmicMutationTracking
MATCH (track:CosmicMutationTracking) WHERE track.mutation_id IS NOT NULL
MATCH (mut:CosmicMutation) WHERE mut.mutation_id = track.mutation_id
MERGE (mut)-[r1:HAS_MUTATION_TRACKING]->(track)
;

// CosmicGene -> CosmicMutationTracking
MATCH (track:CosmicMutationTracking) WHERE track.gene_symbol IS NOT NULL
MATCH (gene:CosmicGene) WHERE gene.gene_symbol = track.gene_symbol
MERGE (gene)-[r2:HAS_MUTATION_TRACKING]->(track)
;

// CosmicNCV -> SampleMutationCollection
MATCH(ncv:CosmicNCV)
MATCH(smc:SampleMutationCollection) WHERE smc.sample_id = ncv.sample_id
MERGE (smc)-[r:HAS_MUTATION]->(ncv)
;

// CosmicResistanceMutation -> SampleMutationCollection
MATCH (resist:CosmicResistanceMutation) WHERE resist.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = resist.sample_id
MERGE (smc) -[r2:HAS_DRUG_RESISTANCE_MUTATION] ->(resist)
;

// CosmicResistanceMutation -> GeneMutationCollection
MATCH (resist:CosmicResistanceMutation) WHERE resist.gene_symbol IS NOT NULL AND resist.census_gene = 'YES'
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = resist.gene_symbol
MERGE (gmc)-[r3:HAS_DRUG_RESISTANCE_MUTATION] ->(resist)
;

// CosmicSample -> CosmicSampleClassification
CALL {
MATCH(cs:CosmicSample)
  WHERE cs.cosmic_phenotype_id IS NOT NULL
MATCH(cc:CosmicSampleClassification)
  WHERE cc.phenotype_id = cs.cosmic_phenotype_id
MERGE (cs)-[r:HAS_SAMPLE_CLASSIFICATION]->(cc)
} IN TRANSACTIONS OF 3000 ROWS;


// CosmicStruct -> SampleMutationCollection
MATCH (struct:CosmicStruct) WHERE struct.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = struct.sample_id
MERGE (smc)-[r1:HAS_STRUCT_MUTATION]->(struct)
;