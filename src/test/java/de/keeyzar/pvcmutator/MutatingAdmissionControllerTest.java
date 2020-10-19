package de.keeyzar.pvcmutator;

import io.fabric8.kubernetes.api.model.admission.AdmissionReview;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class MutatingAdmissionControllerTest {

    @Test
    void validateNoMutationWhenReadWriteOnce() throws IOException {
        InputStream jsonFromFileAsInputStream = getClass().getClassLoader().getResourceAsStream("pvc-readwriteonce.json");
        AdmissionReview admissionReview = new AdmissionReviewMessageBodyReader().readFrom(null, null, null, null, null, jsonFromFileAsInputStream);
        Jsonb jsonb = JsonbBuilder.create(new JsonbConfig());

        //this is necessary, because deserializing with jsonb formatter creates.. rather uncool side effects.. :)
        new QuantityDeserializeFixer().fixDeserializeError(admissionReview);

        final String expectedResponse = "{\"additionalProperties\":{},\"apiVersion\":\"admission.k8s.io/v1\",\"kind\":\"AdmissionReview\",\"response\":{\"additionalProperties\":{},\"allowed\":true,\"uid\":\"b732106d-543e-4701-817b-9f9685bc79cf\"}}";
        String response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(jsonb.toJson(admissionReview))

                .when().post("/mutate")

                .then()
                .statusCode(200)
                .extract().asString();

        MatcherAssert.assertThat(response, is(expectedResponse));
    }

    @Test
    void validateReadWriteManyMutationToNewStorageClass() throws IOException {
        InputStream jsonFromFileAsInputStream = getClass().getClassLoader().getResourceAsStream("pvc-readwritemany.json");
        AdmissionReview admissionReview = new AdmissionReviewMessageBodyReader().readFrom(null, null, null, null, null, jsonFromFileAsInputStream);
        Jsonb jsonb = JsonbBuilder.create(new JsonbConfig());

        //this is necessary, because deserializing with jsonb formatter creates.. rather uncool sideeffects.. :)
        new QuantityDeserializeFixer().fixDeserializeError(admissionReview);

        //this is patch with replace op for new storage class (encoded..)
        final String expectedResponse = "{\"additionalProperties\":{},\"apiVersion\":\"admission.k8s.io/v1\",\"kind\":\"AdmissionReview\",\"response\":{\"additionalProperties\":{},\"allowed\":true,\"patch\":\"W3sib3AiOiJyZXBsYWNlIiwicGF0aCI6Ii9zcGVjL3N0b3JhZ2VDbGFzc05hbWUiLCJ2YWx1ZSI6Im5mcy1jbGllbnQifV0=\",\"patchType\":\"JSONPatch\",\"uid\":\"b732106d-543e-4701-817b-9f9685bc79cf\"}}";
        String response = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(jsonb.toJson(admissionReview))

                .when().post("/mutate")

                .then()
                .statusCode(200)
                .extract().asString();

        MatcherAssert.assertThat(response, is(expectedResponse));
    }

}