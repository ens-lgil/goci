/*
################################################################################
Migration script to update CATALOG_SUMMARY_VIEW with new columns required for
mapping pipeline

Designed for execution with Flyway database migrations tool; this should be
automatically run to completely generate the schema that is out-of-the-box
compatibile with the GOCI model (see
https://github.com/tburdett/goci/tree/2.x-dev/goci-core/goci-model for more).

author:  Emma Hastings
date:     August 5th 2015
version: 2.0.1.028
################################################################################
*/
--------------------------------------------------------
--  Regenerate CATALOG_SUMMARY_VIEW
--------------------------------------------------------

CREATE OR REPLACE VIEW CATALOG_SUMMARY_VIEW (
  ID,
  STUDY_ADDED_DATE,
  PUBMED_ID,
  AUTHOR,
  PUBLICATION_DATE,
  JOURNAL,
  LINK,
  STUDY,
  DISEASE_TRAIT,
  EFO_TRAIT,
  EFO_URI,
  INITIAL_SAMPLE_DESCRIPTION,
  REPLICATE_SAMPLE_DESCRIPTION,
  REGION,
  CHROMOSOME_NAME,
  CHROMOSOME_POSITION,
  REPORTED_GENE,
  MAPPED_GENE,
  ENTREZ_GENE_ID,
  ENSEMBL_GENE_ID,
  MAPPED_GENE_SOURCE,
  UPSTREAM_MAPPED_GENE,
  UPSTREAM_ENTREZ_GENE_ID,
  UPSTEAM_ENSEMBL_GENE_ID,
  UPSTREAM_GENE_DISTANCE,
  UPSTREAM_GENE_SOURCE,
  UPSTREAM_IS_CLOSEST_GENE,
  DOWNSTREAM_MAPPED_GENE,
  DOWNSTREAM_ENTREZ_GENE_ID,
  DOWNSTREAM_ENSEMBL_GENE_ID,
  DOWNSTREAM_GENE_DISTANCE,
  DOWNSTREAM_GENE_SOURCE,
  DOWNSTREAM_IS_CLOSEST,
  STRONGEST_SNP_RISK_ALLELE,
  SNP_RSID,
  MERGED,
  SNP_ID,
  CONTEXT,
  IS_INTERGENIC,
  RISK_ALLELE_FREQUENCY,
  P_VALUE_MANTISSA,
  P_VALUE_EXPONENT,
  P_VALUE_QUALIFIER,
  OR_BETA,
  CI,
  CI_QUALIFIER,
  PLATFORM,
  CNV,
  ASSOCIATION_ID,
  STUDY_ID,
  CATALOG_PUBLISH_DATE,
  CATALOG_UNPUBLISH_DATE,
  CURATION_STATUS)
  AS SELECT ROWNUM, V.* FROM
  (SELECT
  h.STUDY_ADDED_DATE,
  s.PUBMED_ID,
  s.AUTHOR,
  s.PUBLICATION_DATE,
  s.PUBLICATION AS JOURNAL,
  CONCAT('http://europepmc.org/abstract/MED/', s.PUBMED_ID) AS LINK,
  s.TITLE AS STUDY,
  dt.TRAIT AS DISEASE_TRAIT,
  et.TRAIT AS EFO_TRAIT,
  et.URI AS EFO_URI,
  s.INITIAL_SAMPLE_SIZE AS INITIAL_SAMPLE_DESCRIPTION,
  s.REPLICATE_SAMPLE_SIZE AS REPLICATE_SAMPLE_DESCRIPTION,
  r.NAME AS REGION,
  loc.CHROMOSOME_NAME,
  loc.CHROMOSOME_POSITION,
  rg.GENE_NAME AS REPORTED_GENE,
  img.GENE_NAME AS MAPPED_GENE,
  ieg.ENTREZ_GENE_ID,
  ieng.ENSEMBL_GENE_ID,
  igc.SOURCE AS MAPPED_GENE_SOURCE,
  umg.GENE_NAME AS UPSTREAM_MAPPED_GENE,
  ueg.ENTREZ_GENE_ID AS UPSTREAM_ENTREZ_GENE_ID,
  ueng.ENSEMBL_GENE_ID AS UPSTEAM_ENSEMBL_GENE_ID,
  ugc.DISTANCE AS UPSTREAM_GENE_DISTANCE,
  ugc.SOURCE AS UPSTREAM_GENE_SOURCE,
  ugc.IS_CLOSEST_GENE AS UPSTREAM_IS_CLOSEST_GENE,
  dmg.GENE_NAME AS DOWNSTREAM_MAPPED_GENE,
  deg.ENTREZ_GENE_ID AS DOWNSTREAM_ENTREZ_GENE_ID,
  deng.ENSEMBL_GENE_ID AS DOWNSTREAM_ENSEMBL_GENE_ID,
  dgc.DISTANCE AS DOWNSTREAM_GENE_DISTANCE,
  dgc.SOURCE AS DOWNSTREAM_GENE_SOURCE,
  dgc.IS_CLOSEST_GENE AS DOWNSTREAM_IS_CLOSEST,
  ra.RISK_ALLELE_NAME AS STRONGEST_SNP_RISK_ALLELE,
  snp.RS_ID AS SNP_RS_ID,
  snp.MERGED,
  snp.ID AS SNP_ID,
  snp.FUNCTIONAL_CLASS AS CONTEXT,
  (CASE WHEN igc.IS_INTERGENIC IS NOT NULL THEN igc.IS_INTERGENIC ELSE ugc.IS_INTERGENIC END) AS IS_INTERGENIC,
  a.RISK_FREQUENCY AS RISK_ALLELE_FREQUENCY,
  a.PVALUE_MANTISSA AS P_VALUE_MANTISSA,
  a.PVALUE_EXPONENT AS P_VALUE_EXPONENT,
  a.PVALUE_TEXT AS P_VALUE_QUALIFIER,
  a.OR_PER_COPY_NUM AS OR_BETA,
  a.OR_PER_COPY_RANGE AS CI,
  a.OR_PER_COPY_UNIT_DESCR AS CI_QUALIFIER,
  s.PLATFORM,
  s.CNV,
  a.ID AS ASSOCIATION_ID,
  s.ID AS STUDY_ID,
  h.CATALOG_PUBLISH_DATE,
  h.CATALOG_UNPUBLISH_DATE,
  cs.STATUS as CURATION_STATUS
  FROM STUDY s
  JOIN HOUSEKEEPING h ON h.ID = s.HOUSEKEEPING_ID
  JOIN CURATION_STATUS cs ON h.CURATION_STATUS_ID = cs.ID
  LEFT JOIN STUDY_DISEASE_TRAIT sdt ON sdt.STUDY_ID = s.ID
  LEFT JOIN DISEASE_TRAIT dt ON dt.ID = sdt.DISEASE_TRAIT_ID
  LEFT JOIN STUDY_EFO_TRAIT seft ON seft.STUDY_ID = s.ID
  LEFT JOIN EFO_TRAIT et ON et.ID = seft.EFO_TRAIT_ID
  LEFT JOIN ASSOCIATION a ON a.STUDY_ID = s.ID
  LEFT JOIN ASSOCIATION_LOCUS al ON al.ASSOCIATION_ID = a.ID
  LEFT JOIN LOCUS_RISK_ALLELE lra ON lra.LOCUS_ID = al.LOCUS_ID
  LEFT JOIN RISK_ALLELE ra ON ra.ID = lra.RISK_ALLELE_ID
  LEFT JOIN RISK_ALLELE_SNP ras ON ras.RISK_ALLELE_ID = lra.RISK_ALLELE_ID
  LEFT JOIN SINGLE_NUCLEOTIDE_POLYMORPHISM snp ON snp.ID = ras.SNP_ID
  LEFT JOIN SNP_LOCATION ls ON ls.SNP_ID = snp.ID
  LEFT JOIN LOCATION loc ON ls.LOCATION_ID = loc.id
  LEFT JOIN REGION r ON r.ID = loc.REGION_ID
  LEFT JOIN AUTHOR_REPORTED_GENE arg ON arg.LOCUS_ID = al.LOCUS_ID
  LEFT JOIN GENE rg ON rg.ID = arg.REPORTED_GENE_ID
  LEFT JOIN GENOMIC_CONTEXT ugc ON ugc.SNP_ID = snp.ID AND ugc.IS_UPSTREAM = '1'
  LEFT JOIN GENOMIC_CONTEXT dgc ON dgc.SNP_ID = snp.ID AND dgc.IS_DOWNSTREAM = '1'
  LEFT JOIN GENOMIC_CONTEXT igc ON igc.SNP_ID = snp.ID AND igc.IS_INTERGENIC = '0'
  LEFT JOIN GENE umg ON umg.ID = ugc.GENE_ID
  LEFT JOIN GENE dmg ON dmg.ID = dgc.GENE_ID
  LEFT JOIN GENE img ON img.ID = igc.GENE_ID
  LEFT JOIN GENE_ENTREZ_GENE ugeg ON ugeg.GENE_ID = umg.ID
  LEFT JOIN GENE_ENTREZ_GENE dgeg ON dgeg.GENE_ID = dmg.ID
  LEFT JOIN GENE_ENTREZ_GENE igeg ON igeg.GENE_ID = img.ID
  LEFT JOIN ENTREZ_GENE ueg ON ueg.ID = ugeg.ENTREZ_GENE_ID
  LEFT JOIN ENTREZ_GENE ieg ON ieg.ID = igeg.ENTREZ_GENE_ID
  LEFT JOIN ENTREZ_GENE deg ON deg.ID = dgeg.ENTREZ_GENE_ID
  LEFT JOIN GENE_ENSEMBL_GENE ugeng ON ugeng.GENE_ID = umg.ID
  LEFT JOIN GENE_ENSEMBL_GENE dgeng ON dgeng.GENE_ID = dmg.ID
  LEFT JOIN GENE_ENSEMBL_GENE igeng ON igeng.GENE_ID = img.ID
  LEFT JOIN ENSEMBL_GENE ueng ON ueng.ID = ugeng.ENSEMBL_GENE_ID
  LEFT JOIN ENSEMBL_GENE ieng ON ieng.ID = igeng.ENSEMBL_GENE_ID
  LEFT JOIN ENSEMBL_GENE deng ON deng.ID = dgeng.ENSEMBL_GENE_ID
  ORDER BY s.PUBLICATION_DATE DESC, CHROMOSOME_NAME ASC, CHROMOSOME_POSITION ASC) V