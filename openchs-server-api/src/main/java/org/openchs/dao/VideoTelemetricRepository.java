package org.openchs.dao;

import org.openchs.domain.VideoTelemetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestResource(collectionResourceRel = "videotelemetric", path = "videotelemetric", exported = false)
@PreAuthorize(value = "hasAnyAuthority('user', 'organisation_admin')")
public interface VideoTelemetricRepository extends JpaRepository<VideoTelemetric, Long> {

    VideoTelemetric findByUuid(String uuid);
}
