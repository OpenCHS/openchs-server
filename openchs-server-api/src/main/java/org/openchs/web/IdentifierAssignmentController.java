package org.openchs.web;

import org.joda.time.DateTime;
import org.openchs.dao.IdentifierAssignmentRepository;
import org.openchs.dao.IdentifierSourceRepository;
import org.openchs.dao.IndividualRepository;
import org.openchs.dao.ProgramEnrolmentRepository;
import org.openchs.domain.IdentifierAssignment;
import org.openchs.domain.User;
import org.openchs.service.IdentifierAssignmentService;
import org.openchs.service.UserService;
import org.openchs.web.request.IdentifierAssignmentRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@Transactional
public class IdentifierAssignmentController extends AbstractController<IdentifierAssignment> implements RestControllerResourceProcessor<IdentifierAssignment> {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(IndividualController.class);
    private IdentifierAssignmentRepository identifierAssignmentRepository;
    private UserService userService;
    private IdentifierAssignmentService identifierAssignmentService;
    private IndividualRepository individualRepository;
    private ProgramEnrolmentRepository programEnrolmentRepository;

    @Autowired
    public IdentifierAssignmentController(IdentifierAssignmentRepository identifierAssignmentRepository, IdentifierSourceRepository identifierSourceRepository, UserService userService, IdentifierAssignmentService identifierAssignmentService, IndividualRepository individualRepository, ProgramEnrolmentRepository programEnrolmentRepository) {
        this.identifierAssignmentRepository = identifierAssignmentRepository;
        this.userService = userService;
        this.identifierAssignmentService = identifierAssignmentService;
        this.individualRepository = individualRepository;
        this.programEnrolmentRepository = programEnrolmentRepository;
    }

    /**
     * This method generates identifiers on the fly if there are missing identifiers.
     * Due to this, we do not pass the <code>now</code> parameter that is generated in
     * <code>TransactionalResourceInterceptor</code>.
     *
     * This approach is fine because we do not expect IdentifierAssignments to be shared across users.
     *
     * @param lastModifiedDateTime
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/identifierAssignment", method = RequestMethod.GET)
    @PreAuthorize(value = "hasAnyAuthority('user', 'organisation_admin')")
    @Transactional
    public PagedModel<EntityModel<IdentifierAssignment>> get(
            @RequestParam("lastModifiedDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime lastModifiedDateTime,
            Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        identifierAssignmentService.generateIdentifiersIfNecessary(currentUser);

        return wrap(identifierAssignmentRepository.findByAssignedToAndAuditLastModifiedDateTimeGreaterThanAndIsVoidedFalseAndIndividualIsNullAndProgramEnrolmentIsNullOrderByAssignmentOrderAsc(currentUser, lastModifiedDateTime, pageable));
    }

    @RequestMapping(value = "/identifierAssignments", method = RequestMethod.POST)
    @Transactional
    @PreAuthorize(value = "hasAnyAuthority('user', 'organisation_admin')")
    public void save(@RequestBody IdentifierAssignmentRequest identifierAssignmentRequest) {
        logger.info(String.format("Saving identifierAssignment with UUID %s", identifierAssignmentRequest.getUuid()));

        IdentifierAssignment identifierAssignment = createIdentifierAssignment(identifierAssignmentRequest);

        identifierAssignmentRepository.save(identifierAssignment);
        logger.info(String.format("Saved identifierAssignment with UUID %s", identifierAssignmentRequest.getUuid()));
    }

    private IdentifierAssignment createIdentifierAssignment(IdentifierAssignmentRequest identifierAssignmentRequest) {
        IdentifierAssignment identifierAssignment = identifierAssignmentRepository.findByUuid(identifierAssignmentRequest.getUuid());
        if (identifierAssignmentRequest.getIndividualUUID() != null) {
            identifierAssignment.setIndividual(individualRepository.findByUuid(identifierAssignmentRequest.getIndividualUUID()));
        }

        if (identifierAssignmentRequest.getProgramEnrolmentUUID() != null) {
            identifierAssignment.setProgramEnrolment(programEnrolmentRepository.findByUuid(identifierAssignmentRequest.getProgramEnrolmentUUID()));
        }

        return identifierAssignment;
    }

    @Override
    public EntityModel<IdentifierAssignment> process(EntityModel<IdentifierAssignment> resource) {
        IdentifierAssignment identifierAssignment = resource.getContent();
        resource.removeLinks();
        if (identifierAssignment.getProgramEnrolment() != null) {
            resource.add(new Link(identifierAssignment.getProgramEnrolment().getUuid(), "programEnrolmentUUID"));
        }
        if (identifierAssignment.getIndividual() != null) {
            resource.add(new Link(identifierAssignment.getIndividual().getUuid(), "individualUUID"));
        }
        resource.add(new Link(identifierAssignment.getIdentifierSource().getUuid(), "identifierSourceUUID"));
        return resource;
    }
}