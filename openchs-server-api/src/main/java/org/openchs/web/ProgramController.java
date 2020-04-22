package org.openchs.web;

import org.openchs.application.Form;
import org.openchs.application.FormMapping;
import org.openchs.application.FormType;
import org.openchs.dao.*;
import org.openchs.dao.application.FormMappingRepository;
import org.openchs.dao.application.FormRepository;
import org.openchs.domain.*;
import org.openchs.service.ProgramService;
import org.openchs.util.ApiException;
import org.openchs.util.ReactAdminUtil;
import org.openchs.web.request.ProgramRequest;
import org.openchs.web.request.webapp.EncounterTypeContractWeb;
import org.openchs.web.request.webapp.ProgramContractWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProgramController implements RestControllerResourceProcessor<ProgramContractWeb> {
    private final Logger logger;
    private ProgramRepository programRepository;
    private OperationalProgramRepository operationalProgramRepository;
    private ProgramService programService;
    private final IndividualRepository individualRepository;
    private FormMappingRepository formMappingRepository;
    private FormRepository formRepository;
    private SubjectTypeRepository subjectTypeRepository;

    @Autowired
    public ProgramController(ProgramRepository programRepository, OperationalProgramRepository operationalProgramRepository, ProgramService programService, IndividualRepository individualRepository, FormMappingRepository formMappingRepository, FormRepository formRepository, SubjectTypeRepository subjectTypeRepository) {
        this.programRepository = programRepository;
        this.operationalProgramRepository = operationalProgramRepository;
        this.programService = programService;
        this.individualRepository = individualRepository;
        this.formMappingRepository = formMappingRepository;
        this.formRepository = formRepository;
        this.subjectTypeRepository = subjectTypeRepository;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @RequestMapping(value = "/programs", method = RequestMethod.POST)
    @Transactional
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    public void save(@RequestBody List<ProgramRequest> programRequests) {
        programRequests.forEach(programRequest -> {
            programService.saveProgram(programRequest);
        });
    }

    @PostMapping(value = "/web/program")
    @PreAuthorize(value = "hasAnyAuthority('organisation_admin')")
    @Transactional
    ResponseEntity saveProgramForWeb(@RequestBody ProgramContractWeb request) {
        Program existingProgram =
                programRepository.findByNameIgnoreCase(request.getName());
        OperationalProgram existingOperationalProgram =
                operationalProgramRepository.findByNameIgnoreCase(request.getName());
        if (existingProgram != null || existingOperationalProgram != null)
            return ResponseEntity.badRequest().body(ReactAdminUtil.generateJsonError(String.format("Program %s already exists", request.getName())));
        Program program = new Program();
        program.assignUUIDIfRequired();
        program.setName(request.getName());
        program.setColour(request.getColour());
        program.setEnrolmentSummaryRule(request.getEnrolmentSummaryRule());
        program.setEnrolmentEligibilityCheckRule(request.getEnrolmentEligibilityCheckRule());
        programRepository.save(program);
        OperationalProgram operationalProgram = new OperationalProgram();
        operationalProgram.assignUUIDIfRequired();
        operationalProgram.setName(request.getName());
        operationalProgram.setProgramSubjectLabel(request.getProgramSubjectLabel());
        operationalProgram.setProgram(program);
        operationalProgramRepository.save(operationalProgram);

        saveFormMapping(request, program,
                request.getProgramEnrolmentFormUuid(), FormType.ProgramEnrolment,
                formRepository.findByUuid(request.getProgramEnrolmentFormUuid()));

        saveFormMapping(request, program,
                request.getProgramExitFormUuid(), FormType.ProgramExit,
                formRepository.findByUuid(request.getProgramExitFormUuid()));

        return ResponseEntity.ok(ProgramContractWeb.fromOperationalProgram(operationalProgram));
    }

    @PutMapping(value = "/web/program/{id}")
    @PreAuthorize(value = "hasAnyAuthority('organisation_admin')")
    @Transactional
    public ResponseEntity updateProgramForWeb(@RequestBody ProgramContractWeb request,
                                              @PathVariable("id") Long id) {
        logger.info(String.format("Processing Operational Program update request: %s", request.toString()));
        if (request.getName().trim().equals(""))
            return ResponseEntity.badRequest().body(ReactAdminUtil.generateJsonError("Name can not be empty"));

        OperationalProgram operationalProgram = operationalProgramRepository.findOne(id);

        if (operationalProgram == null)
            return ResponseEntity.badRequest()
                    .body(ReactAdminUtil.generateJsonError(String.format("Operational Program with id '%d' not found", id)));

        Program program = operationalProgram.getProgram();

        program.setName(request.getName());
        program.setColour(request.getColour());
        program.setEnrolmentSummaryRule(request.getEnrolmentSummaryRule());
        program.setEnrolmentEligibilityCheckRule(request.getEnrolmentEligibilityCheckRule());

        programRepository.save(program);

        operationalProgram.setProgramSubjectLabel(request.getProgramSubjectLabel());
        operationalProgram.setName(request.getName());
        operationalProgramRepository.save(operationalProgram);

        saveFormMapping(request, program,
                request.getProgramEnrolmentFormUuid(), FormType.ProgramEnrolment,
                formRepository.findByUuid(request.getProgramEnrolmentFormUuid()));

        saveFormMapping(request, program,
                request.getProgramExitFormUuid(), FormType.ProgramExit,
                formRepository.findByUuid(request.getProgramExitFormUuid()));

        return ResponseEntity.ok(ProgramContractWeb.fromOperationalProgram(operationalProgram));
    }

    private void saveFormMapping(ProgramContractWeb request, Program program, String formUuid, FormType formType, Form form) {
        FormMapping formMappingForProgramEncounter;
        if (formUuid != null && !formUuid.isEmpty()) {
            formMappingForProgramEncounter = getOrCreateFormMapping(formType, program, request.getSubjectTypeUuid());
        } else {
            formMappingForProgramEncounter = createFormMapping();
        }

        formMappingForProgramEncounter.setSubjectType(subjectTypeRepository.findByUuid(request.getSubjectTypeUuid()));
        formMappingForProgramEncounter.setProgram(program);
        formMappingForProgramEncounter.setForm(form);

        formMappingRepository.save(formMappingForProgramEncounter);
    }

    private FormMapping getOrCreateFormMapping(FormType formType, Program program, String subjectTypeUuid) {
        FormMapping formMapping = formMappingRepository.getRequiredFormMapping(subjectTypeUuid, program.getUuid(), null, formType);
        if (formMapping == null) {
            formMapping = createFormMapping();
        }
        return formMapping;
    }

    private FormMapping createFormMapping() {
        FormMapping formMappingForProgramEncounter;
        formMappingForProgramEncounter = new FormMapping();
        formMappingForProgramEncounter.assignUUID();
        formMappingForProgramEncounter.setVoided(false);
        return formMappingForProgramEncounter;
    }

    @DeleteMapping(value = "/web/program/{id}")
    @PreAuthorize(value = "hasAnyAuthority('admin', 'organisation_admin')")
    @Transactional
    public ResponseEntity voidProgram(@PathVariable("id") Long id) {
        OperationalProgram operationalProgram = operationalProgramRepository.findOne(id);
        List<FormMapping> formMappings = formMappingRepository.findAllByIsVoidedFalse();      
        if (operationalProgram == null)
            return ResponseEntity.notFound().build();
        Program program = operationalProgram.getProgram();
        if (program == null)
            return ResponseEntity.notFound().build();

        for (int formMapping = 0; formMapping < formMappings.size(); formMapping++) {
            if(formMappings.get(formMapping).getProgramUuid() != null && formMappings.get(formMapping).getProgramUuid().equals(program.getUuid())){
                formMappings.get(formMapping).setVoided(true);
            }
        }

        operationalProgram.setName(ReactAdminUtil.getVoidedName(operationalProgram.getName(), operationalProgram.getId()));
        program.setName(ReactAdminUtil.getVoidedName(program.getName(), program.getId()));
        operationalProgram.setVoided(true);
        program.setVoided(true);
        operationalProgramRepository.save(operationalProgram);
        programRepository.save(program);

        return ResponseEntity.ok(null);
    }

    @GetMapping(value = "/web/program")
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    @ResponseBody
    public PagedResources<Resource<ProgramContractWeb>> getAll(Pageable pageable) {
        return wrap(operationalProgramRepository
                .findPageByIsVoidedFalse(pageable)
                .map(ProgramContractWeb::fromOperationalProgram));
    }

    @GetMapping(value = "web/eligiblePrograms")
    @PreAuthorize(value = "hasAnyAuthority('user')")
    @ResponseBody
    public List<ProgramContractWeb> getEligiblePrograms(@RequestParam String subjectUuid) {
        Individual individual = individualRepository.findByUuid(subjectUuid);
        if (individual == null) throw new ApiException("Subject %s not found", subjectUuid);
        List<Program> eligiblePrograms = programService.getEligiblePrograms(individual);
        List<OperationalProgram> operationalPrograms = new ArrayList<>();
        for (Program p: eligiblePrograms) {
            operationalPrograms.addAll(p.getOperationalPrograms());
        }
        return operationalPrograms.stream()
                .map(operationalProgram -> ProgramContractWeb.fromOperationalProgram(operationalProgram))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/web/programs")
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    @ResponseBody
    public List<OperationalProgram> getAllPrograms() {
        return operationalProgramRepository.findAll();
    }

    @GetMapping(value = "/web/program/{id}")
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    @ResponseBody
    public ResponseEntity getOne(@PathVariable("id") Long id) {
        OperationalProgram operationalProgram = operationalProgramRepository.findOne(id);
        if (operationalProgram.isVoided())
            return ResponseEntity.notFound().build();
        ProgramContractWeb programContractWeb = ProgramContractWeb.fromOperationalProgram(operationalProgram);
        return new ResponseEntity<>(programContractWeb, HttpStatus.OK);
    }

}
