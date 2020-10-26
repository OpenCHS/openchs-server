package org.openchs.web;

import org.openchs.dao.CatchmentRepository;
import org.openchs.dao.IdentifierSourceRepository;
import org.openchs.domain.IdentifierSource;
import org.openchs.service.IdentifierSourceService;
import org.openchs.util.ReactAdminUtil;
import org.openchs.web.request.webapp.IdentifierSourceContractWeb;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
public class IdentifierSourceWebController extends AbstractController<IdentifierSource> implements RestControllerResourceProcessor<IdentifierSourceContractWeb> {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(IndividualController.class);
    private IdentifierSourceRepository identifierSourceRepository;
    private CatchmentRepository catchmentRepository;
    private IdentifierSourceService identifierSourceService;

    @Autowired
    public IdentifierSourceWebController(IdentifierSourceRepository identifierSourceRepository, CatchmentRepository catchmentRepository, IdentifierSourceService identifierSourceService) {
        this.identifierSourceRepository = identifierSourceRepository;
        this.catchmentRepository = catchmentRepository;
        this.identifierSourceService = identifierSourceService;
    }

    @GetMapping(value = "/web/identifierSource/search/findAllById")
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    public PagedModel<EntityModel<IdentifierSourceContractWeb>> findAllById(Long ids, Pageable pageable) {
        Long[] id = {ids};
        return wrap(identifierSourceRepository.findByIdIn(id, pageable).map(IdentifierSourceContractWeb::fromIdentifierSource));
    }


    @GetMapping(value = "/web/identifierSource")
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    @ResponseBody
    public PagedModel<EntityModel<IdentifierSourceContractWeb>> getAll(Pageable pageable) {
        return wrap(identifierSourceRepository.findPageByIsVoidedFalse(pageable).map(IdentifierSourceContractWeb::fromIdentifierSource));
    }

    @GetMapping(value = "/web/identifierSource/{id}")
    @PreAuthorize(value = "hasAnyAuthority('admin','organisation_admin')")
    @ResponseBody
    public ResponseEntity getOne(@PathVariable("id") Long id) {
        IdentifierSource identifierSource = identifierSourceRepository.findOne(id);
        if (identifierSource.isVoided())
            return ResponseEntity.notFound().build();
        return new ResponseEntity<>(IdentifierSourceContractWeb.fromIdentifierSource(identifierSource), HttpStatus.OK);
    }


    @PostMapping(value = "/web/identifierSource")
    @PreAuthorize(value = "hasAnyAuthority('organisation_admin')")
    @Transactional
    ResponseEntity saveProgramForWeb(@RequestBody IdentifierSourceContractWeb request) {
        IdentifierSource identifierSource = identifierSourceService.saveIdSource(request);
        return ResponseEntity.ok(IdentifierSourceContractWeb.fromIdentifierSource(identifierSource));
    }

    @PutMapping(value = "/web/identifierSource/{id}")
    @PreAuthorize(value = "hasAnyAuthority('organisation_admin')")
    @Transactional
    public ResponseEntity updateProgramForWeb(@RequestBody IdentifierSourceContractWeb request,
                                              @PathVariable("id") Long id) {
        IdentifierSource identifierSource = identifierSourceRepository.findOne(id);
        if (identifierSource == null)
            return ResponseEntity.badRequest()
                    .body(ReactAdminUtil.generateJsonError(String.format("Identifier source with id '%d' not found", id)));

        IdentifierSource savedEntity = identifierSourceService.updateIdSource(identifierSource, request);
        return ResponseEntity.ok(IdentifierSourceContractWeb.fromIdentifierSource(savedEntity));
    }

    @DeleteMapping(value = "/web/identifierSource/{id}")
    @PreAuthorize(value = "hasAnyAuthority('admin', 'organisation_admin')")
    @Transactional
    public ResponseEntity voidProgram(@PathVariable("id") Long id) {
        IdentifierSource identifierSource = identifierSourceRepository.findOne(id);
        if (identifierSource == null)
            return ResponseEntity.notFound().build();

        identifierSource.setVoided(true);
        identifierSourceRepository.save(identifierSource);
        return ResponseEntity.ok(null);
    }
}
