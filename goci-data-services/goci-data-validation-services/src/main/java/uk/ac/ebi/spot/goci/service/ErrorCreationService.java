package uk.ac.ebi.spot.goci.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.goci.component.ValidationChecks;
import uk.ac.ebi.spot.goci.model.Association;
import uk.ac.ebi.spot.goci.model.AssociationUploadRow;
import uk.ac.ebi.spot.goci.model.Gene;
import uk.ac.ebi.spot.goci.model.RiskAllele;
import uk.ac.ebi.spot.goci.model.ValidationError;
import uk.ac.ebi.spot.goci.utils.ErrorProcessingService;

/**
 * Created by emma on 30/03/2016.
 *
 * @author emma
 *         <p>
 *         Runs checks and creates error messages that can then by returned to client
 */
@Service
public class ErrorCreationService {

    private ValidationChecks validationChecks;

    @Autowired
    public ErrorCreationService(ValidationChecks validationChecks) {
        this.validationChecks = validationChecks;
    }

    public ValidationError checkSnpValueIsPresent(AssociationUploadRow row) {
        String errorMessage = validationChecks.checkValueIsPresent(row.getSnp());
        return ErrorProcessingService.createError(errorMessage, "SNP");
    }

    public ValidationError checkStrongestAlleleValueIsPresent(AssociationUploadRow row) {
        String errorMessage = validationChecks.checkValueIsPresent(row.getStrongestAllele());
        return ErrorProcessingService.createError(errorMessage, "Strongest SNP-Risk Allele/Effect Allele");
    }

    public ValidationError checkSnpType(Association association) {
        String errorMessage = validationChecks.checkSnpType(association.getSnpType());
        return ErrorProcessingService.createError(errorMessage, "SNP type");
    }

    public ValidationError checkOrIsPresent(Association association) {
        String errorMessage = validationChecks.checkOrIsPresentAndLessThanOne(association.getOrPerCopyNum());
        return ErrorProcessingService.createError(errorMessage, "OR");
    }

    public ValidationError checkBetaValuesIsEmpty(Association association) {
        String errorMessage = validationChecks.checkBetaValueIsEmpty(association.getBetaNum());
        return ErrorProcessingService.createError(errorMessage, "Beta");
    }

    public ValidationError checkBetaUnitIsEmpty(Association association) {
        String errorMessage = validationChecks.checkBetaUnitIsEmpty(association.getBetaUnit());
        return ErrorProcessingService.createError(errorMessage, "Beta Unit");
    }

    public ValidationError checkBetaDirectionIsEmpty(Association association) {
        String errorMessage = validationChecks.checkBetaDirectionIsEmpty(association.getBetaDirection());
        return ErrorProcessingService.createError(errorMessage, "Beta Direction");
    }

    public ValidationError checkBetaIsPresent(Association association) {
        String errorMessage = validationChecks.checkBetaIsPresent(association.getBetaNum());
        return ErrorProcessingService.createError(errorMessage, "Beta");
    }

    public ValidationError checkBetaUnitIsPresent(Association association) {
        String errorMessage = validationChecks.checkBetaUnitIsPresent(association.getBetaUnit());
        return ErrorProcessingService.createError(errorMessage, "Beta Unit");
    }

    public ValidationError checkBetaDirectionIsPresent(Association association) {
        String errorMessage = validationChecks.checkBetaDirectionIsPresent(association.getBetaDirection());
        return ErrorProcessingService.createError(errorMessage, "Beta Direction");
    }

    public ValidationError checkOrEmpty(Association association) {
        String errorMessage = validationChecks.checkOrEmpty(association.getOrPerCopyNum());
        return ErrorProcessingService.createError(errorMessage, "OR");
    }

    public ValidationError checkOrRecipEmpty(Association association) {
        String errorMessage = validationChecks.checkOrRecipEmpty(association.getOrPerCopyRecip());
        return ErrorProcessingService.createError(errorMessage, "OR reciprocal");
    }

    public ValidationError checkOrPerCopyRecipRange(Association association) {
        String errorMessage = validationChecks.checkOrPerCopyRecipRangeIsEmpty(association.getOrPerCopyRecipRange());
        return ErrorProcessingService.createError(errorMessage,
                                                  "OR reciprocal range");
    }

    public ValidationError checkRangeIsEmpty(Association association) {
        String errorMessage = validationChecks.checkRangeIsEmpty(association.getRange());
        return ErrorProcessingService.createError(errorMessage, "Range");
    }

    public ValidationError checkStandardErrorIsEmpty(Association association) {
        String errorMessage = validationChecks.checkStandardErrorIsEmpty(association.getStandardError());
        return ErrorProcessingService.createError(errorMessage, "Standard Error");
    }

    public ValidationError checkDescriptionIsEmpty(Association association) {
        String errorMessage = validationChecks.checkDescriptionIsEmpty(association.getDescription());
        return ErrorProcessingService.createError(errorMessage,
                                                  "OR/Beta description");
    }

    public ValidationError checkMantissaIsLessThan10(Association association) {
        String errorMessage = validationChecks.checkMantissaIsLessThan10(association.getPvalueMantissa());
        return ErrorProcessingService.createError(errorMessage, "P-value Mantissa");
    }

    public ValidationError checkExponentIsPresent(Association association) {
        String errorMessage = validationChecks.checkExponentIsPresent(association.getPvalueExponent());
        return ErrorProcessingService.createError(errorMessage, "P-value exponent");
    }

    public ValidationError checkGene(Gene gene) {
        String errorMessage = validationChecks.checkGene(gene.getGeneName());
        return ErrorProcessingService.createError(errorMessage, "Gene");
    }

    public ValidationError checkRiskAllele(RiskAllele riskAllele) {
        String errorMessage = validationChecks.checkRiskAllele(riskAllele.getRiskAlleleName());
        return ErrorProcessingService.createError(errorMessage, "Risk Allele");
    }
}