package org.openchs.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.openchs.common.AbstractControllerIntegrationTest;
import org.openchs.dao.IndividualRepository;
import org.openchs.domain.Individual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(value = {"/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/tear-down.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class IndividualControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Autowired
    private IndividualRepository individualRepository;

    private String INDIVIDUAL_UUID = "0a1bf764-4576-4d71-b8ec-25895a113e81";

    @Test
    public void testGetAll() throws Exception {
        ResponseEntity<String> response = template.getForEntity(base.toString() + "/individual",
                String.class);
    }

    @Test
    public void createNewIndividual() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object json = mapper.readValue(this.getClass().getResource("/ref/individual/newIndividual.json"), Object.class);
            template.postForEntity("/individuals", json, Void.class);

            Individual newIndividual = individualRepository.findByUuid(INDIVIDUAL_UUID);
            assertThat(newIndividual).isNotNull();

        } catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void voidExistingIndividual() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object json = mapper.readValue(this.getClass().getResource("/ref/individual/newIndividual.json"), Object.class);
            template.postForEntity("/individuals", json, Void.class);

            Individual newIndividual = individualRepository.findByUuid(INDIVIDUAL_UUID);
            assertThat(newIndividual).isNotNull();
            assertThat(newIndividual.isVoided()).isFalse();

            json = mapper.readValue(this.getClass().getResource("/ref/individual/voidedIndividual.json"), Object.class);
            template.postForEntity("/individuals", json, Void.class);

            Individual voidedIndividual = individualRepository.findByUuid(INDIVIDUAL_UUID);
            assertThat(voidedIndividual).isNotNull();
            assertThat(voidedIndividual.isVoided()).isTrue();

        } catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void unvoidVoidedIndividual() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Object json = mapper.readValue(this.getClass().getResource("/ref/individual/voidedIndividual.json"), Object.class);
            template.postForEntity("/individuals", json, Void.class);

            Individual voidedIndividual = individualRepository.findByUuid(INDIVIDUAL_UUID);
            assertThat(voidedIndividual).isNotNull();
            assertThat(voidedIndividual.isVoided()).isTrue();

            json = mapper.readValue(this.getClass().getResource("/ref/individual/newIndividual.json"), Object.class);
            template.postForEntity("/individuals", json, Void.class);

            Individual newIndividual = individualRepository.findByUuid(INDIVIDUAL_UUID);
            assertThat(newIndividual).isNotNull();
            assertThat(newIndividual.isVoided()).isFalse();

        } catch (IOException e) {
            Assert.fail();
        }
    }

}