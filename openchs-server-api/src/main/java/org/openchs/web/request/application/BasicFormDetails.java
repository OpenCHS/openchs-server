package org.openchs.web.request.application;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.joda.time.DateTime;
import org.openchs.application.Form;
import org.springframework.hateoas.RepresentationModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "uuid", "formType", "programName", "createdDateTime",
        "lastModifiedDate", "createdBy", "lastModifiedBy", "createdByUUID", "lastModifiedByUUID" })
public class BasicFormDetails extends RepresentationModel {

    private String name;
    private String uuid;
    private String formType;
    private String programName;
    private String subjectName;
    private DateTime createDateTime;
    private DateTime lastModifiedDateTime;
    private String createdBy;
    private String lastModifiedBy;
    private String createdByUUID;
    private String lastModifiedByUUID;
    private Long organisationId;
    private Boolean voided;

    public BasicFormDetails(Form form, String programName) {
        this.name = form.getName();
        this.uuid = form.getUuid();
        this.formType = form.getFormType().name();
        this.programName = programName;
        this.createDateTime = form.getAudit().getCreatedDateTime();
        this.lastModifiedDateTime = form.getAudit().getLastModifiedDateTime();
        this.createdBy = form.getAudit().getCreatedBy().getName();
        this.createdByUUID = form.getAudit().getCreatedBy().getUuid();
        this.lastModifiedBy = form.getAudit().getLastModifiedBy().getName();
        this.lastModifiedByUUID = form.getAudit().getLastModifiedBy().getUuid();
        this.organisationId = form.getOrganisationId();
        this.voided = form.isVoided();
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFormType() {
        return formType;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public DateTime getCreateDateTime() {
        return createDateTime;
    }

    public DateTime getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public String getCreatedByUUID() {
        return createdByUUID;
    }

    public String getLastModifiedByUUID() {
        return lastModifiedByUUID;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }
}