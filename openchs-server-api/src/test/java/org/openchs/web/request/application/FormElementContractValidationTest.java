package org.openchs.web.request.application;

import org.junit.Before;
import org.junit.Test;
import org.openchs.application.KeyType;
import org.openchs.application.KeyValue;
import org.openchs.application.KeyValues;
import org.openchs.application.ValueType;
import org.openchs.domain.ConceptDataType;

import static org.junit.Assert.assertEquals;


public class FormElementContractValidationTest {
    private FormElementContract formElementContract;
    private KeyValues keyValues;

    @Before
    public void beforeTest() {
        formElementContract = new FormElementContract();
        formElementContract.setName("foo");
        formElementContract.setDataType(ConceptDataType.Coded.toString());
        keyValues = new KeyValues();
        formElementContract.setKeyValues(keyValues);
    }

    @Test
    public void validate_form_element_contract_with_select_specified() {
        keyValues.add(new KeyValue(KeyType.Select, ValueType.Multi.toString()));
        assertEquals(false, formElementContract.validate().isFailure());
    }

    @Test
    public void validate_form_element_contract_without_select_specified() {
        assertEquals(true, formElementContract.validate().isFailure());
    }
}