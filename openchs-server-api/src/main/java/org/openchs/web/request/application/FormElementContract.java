package org.openchs.web.request.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.openchs.application.FormElement;
import org.openchs.application.Format;
import org.openchs.application.KeyValues;
import org.openchs.web.request.ConceptContract;
import org.openchs.web.request.ReferenceDataContract;
import org.openchs.web.request.FormatContract;
import org.openchs.common.ValidationResult;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "uuid", "isMandatory", "keyValues", "concept", "displayOrder", "type", "rule"})
public class FormElementContract extends ReferenceDataContract {
    private boolean isMandatory;
    private KeyValues keyValues;
    private ConceptContract concept;
    private Double displayOrder;
    private String type;
    private FormatContract validFormat;
    private Long organisationId;
    private String rule;

    public FormElementContract() {
    }

    public FormElementContract(String uuid, String userUUID, String name, boolean isMandatory, KeyValues keyValues, ConceptContract concept, String type, FormatContract validFormat) {
        super(uuid, userUUID, name);
        this.isMandatory = isMandatory;
        this.keyValues = keyValues;
        this.concept = concept;
        this.type = type;
        this.validFormat = validFormat;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    public KeyValues getKeyValues() {
        return keyValues == null ? new KeyValues() : keyValues;
    }

    public void setKeyValues(KeyValues keyValues) {
        this.keyValues = keyValues;
    }

    public Double getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Double displayOrder) {
        this.displayOrder = displayOrder;
    }

    public ValidationResult validate() {
        if (concept == null || concept.getUuid() == null) {
            return ValidationResult.Failure("Concept UUID Not Provided");
        }
        if (concept != null && concept.isCoded() && !typeSpecified())
            return ValidationResult.Failure(String.format("Doesn't specify whether the FormElement=\"%s\" is single or multi select", this.getName()));

        return ValidationResult.Success;
    }

    private boolean typeSpecified() {
        return type != null;
    }

    public ConceptContract getConcept() {
        return concept;
    }

    public void setConcept(ConceptContract concept) {
        this.concept = concept;
    }

    public String getType() {
        return type == null || type.trim().isEmpty() ? FormElement.SINGLE_SELECT : this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "name=" + this.getName() + '\'' +
                "isMandatory=" + isMandatory +
                ", displayOrder=" + displayOrder +
                '}';
    }

    public Format getValidFormat() {
        return validFormat == null ? null : validFormat.toFormat();
    }

    public void setValidFormat(FormatContract validFormat) {
        this.validFormat = validFormat;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public static FormElementContract fromFormElement(FormElement formElement) {
        FormElementContract feContract = new FormElementContract();
        feContract.setName(formElement.getName());
        feContract.setUuid(formElement.getUuid());
        feContract.setDisplayOrder(formElement.getDisplayOrder());
        feContract.setMandatory(formElement.isMandatory());
        feContract.setType(formElement.getType());
        feContract.setKeyValues(formElement.getKeyValues());
        feContract.setValidFormat(FormatContract.fromFormat(formElement.getValidFormat()));
        feContract.setVoided(formElement.isVoided());
        feContract.setRule(formElement.getRule());
        ConceptContract conceptContract = new ConceptContract();
        conceptContract.setName(formElement.getConcept().getName());
        conceptContract.setUuid(formElement.getConcept().getUuid());
        feContract.setConcept(conceptContract);
        return feContract;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isVoided() {
        return super.isVoided();
    }
}