package org.openchs.domain;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.openchs.geo.Point;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@MappedSuperclass
public class AbstractEncounter extends OrganisationAwareEntity {
    @Column
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "encounter_type_id")
    private EncounterType encounterType;
    @Column
    private DateTime earliestVisitDateTime;
    @Column
    private DateTime maxVisitDateTime;
    @Column
    private DateTime encounterDateTime;
    @Column
    @Type(type = "observations")
    private ObservationCollection observations;
    @Column
    private DateTime cancelDateTime;
    @Column
    @Type(type = "observations")
    private ObservationCollection cancelObservations;
    @Type(type = "org.openchs.geo.PointType")
    @Column
    private Point encounterLocation;
    @Type(type = "org.openchs.geo.PointType")
    @Column
    private Point cancelLocation;
    @Column
    private String legacyId;

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public DateTime getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(DateTime encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public ObservationCollection getObservations() {
        return observations;
    }

    public void setObservations(ObservationCollection observations) {
        this.observations = observations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getEarliestVisitDateTime() {
        return earliestVisitDateTime;
    }

    public void setEarliestVisitDateTime(DateTime earliestVisitDateTime) {
        this.earliestVisitDateTime = earliestVisitDateTime;
    }

    public DateTime getMaxVisitDateTime() {
        return maxVisitDateTime;
    }

    public void setMaxVisitDateTime(DateTime maxVisitDateTime) {
        this.maxVisitDateTime = maxVisitDateTime;
    }

    public DateTime getCancelDateTime() {
        return cancelDateTime;
    }

    public void setCancelDateTime(DateTime cancelDateTime) {
        this.cancelDateTime = cancelDateTime;
    }

    public ObservationCollection getCancelObservations() {
        return cancelObservations;
    }

    public void setCancelObservations(ObservationCollection cancelObservations) {
        this.cancelObservations = cancelObservations;
    }

    public Point getEncounterLocation() {
        return encounterLocation;
    }

    public void setEncounterLocation(Point encounterLocation) {
        this.encounterLocation = encounterLocation;
    }

    public Point getCancelLocation() {
        return cancelLocation;
    }

    public void setCancelLocation(Point cancelLocation) {
        this.cancelLocation = cancelLocation;
    }

    public boolean isCompleted() {
        return getEncounterDateTime() != null;
    }

    public boolean matches(String encounterTypeName, String encounterName) {
        return Objects.equals(this.getEncounterType().getName(), encounterTypeName) && Objects.equals(this.getName(), encounterName);
    }

    public boolean dateFallsWithIn(DateTime encounterDateTime) {
        return encounterDateTime.isAfter(this.getEarliestVisitDateTime()) && encounterDateTime.isBefore(this.getMaxVisitDateTime());
    }

    public boolean isEncounteredOrCancelledBetween(DateTime startDate, DateTime endDate) {
        return (getEncounterDateTime() != null && getEncounterDateTime().isAfter(startDate) && getEncounterDateTime().isBefore(endDate)) ||
                (getCancelDateTime() != null && getCancelDateTime().isAfter(startDate) && getCancelDateTime().isBefore(endDate));
    }

    public String getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(String legacyId) {
        this.legacyId = legacyId;
    }
}
