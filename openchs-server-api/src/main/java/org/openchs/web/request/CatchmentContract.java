package org.openchs.web.request;

import org.openchs.domain.Catchment;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTime;


@Relation(collectionRelation = "catchment")
public class CatchmentContract extends ReferenceDataContract {
    private Long id;

    private List<Long> locationIds;

    private String createdBy;
    private String lastModifiedBy;
    private DateTime createdDateTime;
    private DateTime lastModifiedDateTime;

    private List<AddressLevelContract> locations = new ArrayList<>();

    public List<AddressLevelContract> getLocations() {
        return locations;
    }

    public void setLocations(List<AddressLevelContract> locations) {
        this.locations = locations;
    }

    public static CatchmentContract fromEntity(Catchment catchment) {
        CatchmentContract catchmentContract = new CatchmentContract();
        catchmentContract.setId(catchment.getId());
        catchmentContract.setUuid(catchment.getUuid());
        catchmentContract.setName(catchment.getName());
        catchmentContract.setVoided(catchment.isVoided());
        catchmentContract.setLocationIds(catchment.getAddressLevels().stream().map(addressLevel -> addressLevel.getId()).collect(Collectors.toList()));
        catchmentContract.setCreatedBy(catchment.getAudit().getCreatedBy().getUsername());
        catchmentContract.setLastModifiedBy(catchment.getAudit().getLastModifiedBy().getUsername());
        catchmentContract.setCreatedDateTime(catchment.getAudit().getCreatedDateTime());
        catchmentContract.setModifiedDateTime(catchment.getAudit().getLastModifiedDateTime());
        return catchmentContract;
    }

    @Override
    public String toString() {
        return String.format("UUID: %s, Name: %s", this.getUuid(), this.getName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getLocationIds() {
        return locationIds;
    }



    public void setLocationIds(List<Long> locationIds) {
        this.locationIds = locationIds;
    }
    public void setCreatedBy(String username){
        this.createdBy = username;
    }
    public String getCreatedBy(){
        return createdBy;
    }

    public void setLastModifiedBy(String username){
        this.lastModifiedBy = username;
    }

    public String getLastModifiedBy(){
        return lastModifiedBy;
    }


    public void setCreatedDateTime(DateTime createDateTime){
        this.createdDateTime = createDateTime;
    }

    public DateTime getCreatedDateTime(){
        return createdDateTime;
    }

    public void setModifiedDateTime(DateTime lastModifiedDateTime){
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public DateTime getModifiedDateTime(){
        return lastModifiedDateTime;
    }
}
