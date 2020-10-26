package org.openchs.web;

import org.joda.time.DateTime;
import org.openchs.dao.AddressLevelTypeRepository;
import org.openchs.dao.LocationRepository;
import org.openchs.domain.AddressLevel;
import org.openchs.domain.AddressLevelType;
import org.openchs.service.LocationHierarchyService;
import org.openchs.service.OrganisationConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class LocationHierarchyController implements RestControllerResourceProcessor<AddressLevel> {

    private final LocationRepository locationRepository;
    private final Logger logger;
    private final LocationHierarchyService locationHierarchyService;
    private final AddressLevelTypeRepository addressLevelTypeRepository;

    @Autowired
    public LocationHierarchyController(LocationRepository locationRepository, LocationHierarchyService locationHierarchyService, OrganisationConfigService organisationConfigService, AddressLevelTypeRepository addressLevelTypeRepository) {
        this.locationRepository = locationRepository;
        this.locationHierarchyService = locationHierarchyService;
        this.addressLevelTypeRepository = addressLevelTypeRepository;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @GetMapping(value = "/locationHierarchy/search/lastModified")
    @PreAuthorize(value = "hasAnyAuthority('user','admin','organisation_admin')")
    @ResponseBody
    public PagedModel<EntityModel<AddressLevel>> getAddressLevels(
            @RequestParam("lastModifiedDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) DateTime lastModifiedDateTime,
            Pageable pageable) {
        try {
            ArrayList<Long> addressLevelTypeIds = (ArrayList<Long>) locationHierarchyService.getLowestAddressLevelTypeHierarchiesForOrganisation();
            if (addressLevelTypeIds != null) {
                List<AddressLevelType> addressLevelTypes = addressLevelTypeRepository.findAllByIdIn(addressLevelTypeIds);
                return wrap(locationRepository.findByAuditLastModifiedDateTimeAfterAndTypeIn(lastModifiedDateTime, addressLevelTypes, pageable));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error(exception.getMessage());
            return null;
        }
        return wrap(new PageImpl<>(Collections.emptyList()));
    }
}
