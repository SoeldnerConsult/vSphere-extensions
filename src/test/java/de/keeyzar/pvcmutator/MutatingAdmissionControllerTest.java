package de.keeyzar.pvcmutator;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class MutatingAdmissionControllerTest {

    @Test
    void validateNoMutationWhenReadWriteOnce() {
        InputStream jsonFromFileAsInputStream = getClass().getClassLoader().getResourceAsStream("pvcmutator/pvc-readwriteonce.json");

        final String expectedResponse = "{\"additionalProperties\":{},\"apiVersion\":\"admission.k8s.io/v1\",\"kind\":\"AdmissionReview\",\"response\":{\"additionalProperties\":{},\"allowed\":true,\"uid\":\"b732106d-543e-4701-817b-9f9685bc79cf\"}}";
        String response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(jsonFromFileAsInputStream)

                .when().post("/pvc/mutate")

                .then()
                .statusCode(200)
                .extract().asString();

        MatcherAssert.assertThat(response, is(expectedResponse));
    }

    @Test
    void validateReadWriteManyMutationToNewStorageClass() {
        InputStream jsonFromFileAsInputStream = getClass().getClassLoader().getResourceAsStream("pvcmutator/pvc-readwritemany.json");

        //this is patch with replace op for new storage class (encoded..)
        final String expectedResponse = "{\"additionalProperties\":{},\"apiVersion\":\"admission.k8s.io/v1\",\"kind\":\"AdmissionReview\",\"response\":{\"additionalProperties\":{},\"allowed\":true,\"patch\":\"W3sib3AiOiJyZXBsYWNlIiwicGF0aCI6Ii9zcGVjL3N0b3JhZ2VDbGFzc05hbWUiLCJ2YWx1ZSI6IlwidG8tYmUtb3ZlcnJpZGRlblwiIn1d\",\"patchType\":\"JSONPatch\",\"uid\":\"b732106d-543e-4701-817b-9f9685bc79cf\"}}";
        String response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(jsonFromFileAsInputStream)

                .when().post("/pvc/mutate")

                .then()
                .statusCode(200)
                .extract().asString();

        MatcherAssert.assertThat(response, is(expectedResponse));
    }

}