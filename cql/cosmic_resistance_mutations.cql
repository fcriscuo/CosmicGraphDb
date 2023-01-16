:auto LOAD CSV WITH HEADERS FROM
'file:///Volumes/SSD870/COSMIC_rel97/CosmicResistanceMutations.tsv' AS line FIELDTERMINATOR '\t'
CALL {
WITH line
MERGE (resist:CosmicResistanceMutation:Mutation {resist_identifier: apoc.create.uuid()})

SET resist.sample_name = line.`Sample Name`,
resist.sample_id = toInteger(line.`Sample ID`),
resist.gene_symbol = line.`Gene Name`,
resist.transcript = line.Transcript,
resist.census_gene = toUpper(line.`Census Gene`),
resist.drug_name = line.`Gene	Drug`,
resist.mutation_id = toInteger(line.MUTATION_ID),
resist.genomic_mutation_id = line.GENOMIC_MUTATION_ID,
resist.legacy_mutation_id = line.LEGACY_MUTATION_ID,
resist.aa_mutation = line.`AA Mutation`,
resist.cds_mutation = line.`CDS Mutation`,
resist.pub_id = toInteger(line.`Pubmed Id`),
resist.cgp_study = line.`CGP Study`,
resist.somatic_status = line.`Somatic Status`,
resist.sample_type = line.`Sample Type`,
resist.zygosity = line.Zygosity,
resist.genome_coordinates = line.`Genome Coordinates (GRCh38)	Tier`,
resist.tier = toInteger(line.Tier),
resist.hgvsp = line.HGVSP,
resist.hgvsc = line.HGVSC,
resist.hgvsg = line.HGVSG,
resist.data_created = datetime()
} IN TRANSACTIONS OF 3000 ROWS;

// relationship to PubMed
MATCH(resist:CosmicResistanceMutation) WHERE resist.pub_id IS NOT NULL
MERGE(p:Publication{ pub_id:toInteger(resist.pub_id)})
SET p.url= genomiccore.resolvePubmedUrl(resist.pub_id)
MERGE (resist)-[r1:HAS_PUBLICATION]->(p)
;

// relationship to SampleMutationCollection
MATCH (resist:CosmicResistanceMutation) WHERE resist.sample_id IS NOT NULL
MATCH (smc:SampleMutationCollection) WHERE smc.sample_id = resist.sample_id
MERGE (smc) -[r2:HAS_DRUG_RESISTANCE_MUTATION] ->(resist)
;

// relationship to GeneMutationCollection for Census genes
MATCH (resist:CosmicResistanceMutation) WHERE resist.gene_symbol IS NOT NULL AND resist.census_gene = 'YES'
MATCH (gmc:GeneMutationCollection) WHERE gmc.gene_symbol = resist.gene_symbol
MERGE (gmc)-[r3:HAS_DRUG_RESISTANCE_MUTATION] ->(resist)
;



