package org.openchs.service;

import org.openchs.application.Form;
import org.openchs.application.FormType;
import org.openchs.application.KeyType;
import org.openchs.builder.FormBuilder;
import org.openchs.builder.FormBuilderException;
import org.openchs.dao.ConceptRepository;
import org.openchs.dao.application.FormRepository;
import org.openchs.domain.Concept;
import org.openchs.domain.ConceptDataType;
import org.openchs.web.request.application.FormContract;
import org.openchs.web.request.application.FormElementContract;
import org.openchs.web.request.application.FormElementGroupContract;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class FormService {

    private FormRepository formRepository;
    private OrganisationConfigService organisationConfigService;
    private ConceptRepository conceptRepository;

    public FormService(FormRepository formRepository, OrganisationConfigService organisationConfigService, ConceptRepository conceptRepository) {
        this.formRepository = formRepository;
        this.organisationConfigService = organisationConfigService;
        this.conceptRepository = conceptRepository;
    }

    public void saveForm(FormContract formRequest) throws FormBuilderException {
        Form existingForm = formRepository.findByUuid(formRequest.getUuid());
        FormBuilder formBuilder = new FormBuilder(existingForm);
        Form form = formBuilder.withName(formRequest.getName())
                .withType(formRequest.getFormType())
                .withUUID(formRequest.getUuid())
                .withFormElementGroups(formRequest.getFormElementGroups())
                .withDecisionRule(formRequest.getDecisionRule())
                .withVisitScheduleRule(formRequest.getVisitScheduleRule())
                .withValidationRule(formRequest.getValidationRule())
                .withChecklistRule(formRequest.getChecklistsRule())
                .withVoided(formRequest.isVoided())
                .build();

        mapDecisionConcepts(formRequest, form);
        //Form audit values might not change for changes in form element groups or form elements.
        //This updateAudit forces audit updates
        form.updateAudit();
        formRepository.save(form);
    }

    private void mapDecisionConcepts(FormContract formRequest, Form form) {
        formRequest.getDecisionConcepts().forEach(conceptContract -> {
            if (!form.hasDecisionConcept(conceptContract.getId())) {
                form.addDecisionConcept(conceptRepository.findOne(conceptContract.getId()));
            }
        });
        form.getDecisionConcepts().forEach(concept -> {
            if (formRequest.getDecisionConcepts().stream().filter(conceptContract -> conceptContract.getId().equals(concept.getId())).findFirst().orElse(null) == null) {
                form.removeDecisionConcept(concept);
            }
        });
    }

    public Form getOrCreateForm(String formUuid, String formName, FormType formType) {
        Form form = formRepository.findByUuid(formUuid);
        if (form != null) {
            return form;
        }

        form = Form.create();
        form.setName(formName);
        form.assignUUID();
        form.setFormType(formType);
        formRepository.save(form);
        return form;
    }

    public void checkIfLocationConceptsHaveBeenUsed(FormContract formRequest) {
        HashSet<String> locationConceptUuids = new HashSet<>();
        for (FormElementGroupContract formElementGroup : formRequest.getFormElementGroups()) {
            for (FormElementContract formElement : formElementGroup.getFormElements()) {
                if (formElement.getConcept().getDataType() != null && formElement.getConcept().getDataType().equals(String.valueOf(ConceptDataType.Location))) {
                    Concept locationConcept = conceptRepository.findByUuid(formElement.getConcept().getUuid());
                    locationConceptUuids.addAll((ArrayList<String>)locationConcept.getKeyValues().getKeyValue(KeyType.lowestAddressLevelTypeUUIDs).getValue());
                }
            }
        }
        if (!locationConceptUuids.isEmpty()) {
            organisationConfigService.updateLowestAddressLevelTypeSetting(locationConceptUuids);
        }
    }
}
