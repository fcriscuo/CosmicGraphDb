#!/bin/bash
export COSMIC_DATA_HOME=/Volumes/SSD870/COSMIC_rel97
# shellcheck disable=SC2164
if [ $# -eq 0 ]; then
    echo "The number of file rows to be copied must be provided"
    exit 1
fi
cd "$COSMIC_DATA_HOME"
if [ ! -d sample ]; then
  mkdir sample
  echo sample subdirectory created
fi
cp Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv ./sample/Cancer_Gene_Census_Hallmarks_Of_Cancer.tsv
head -$1 CosmicBreakpointsExport.tsv > ./sample/CosmicBreakpointsExport.tsv
head -$1 CosmicCompleteCNA.tsv > ./sample/CosmicCompleteCNA.tsv
head -$1 CosmicCompleteDifferentialMethylation.tsv > ./sample/CosmicCompleteDifferentialMethylation.tsv
head -$1 CosmicCompleteGeneExpression.tsv  > ./sample/CosmicCompleteGeneExpression.tsv
head -$1 CosmicFusionExport.tsv > ./sample/CosmicHGNC.tsv
cp  CosmicHGNC.tsv ./sample/CosmicHGNC.tsv
head -$1 CosmicMutantExport.tsv  > ./sample/CosmicMutantExport.tsv
head -$1 CosmicMutantExportCensus.tsv > ./sample/CosmicMutantExportCensus
head -$1 CosmicNCV.tsv > ./sample/CosmicNCV.tsv
head -$1 CosmicResistanceMutations.tsv > ./sample/CosmicResistanceMutations.tsv
head -$1 CosmicSample.tsv > ./sample/CosmicSample.tsv
head -$1 CosmicStructExport.tsv > ./sample/CosmicStructExport.tsv
cp cancer_gene_census.csv ./sample/cancer_gene_census.csv
cp classification.csv ./sample/classification.csv
ls -l ./sample