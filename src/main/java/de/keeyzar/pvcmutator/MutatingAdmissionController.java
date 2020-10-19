package de.keeyzar.pvcmutator;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.admission.AdmissionRequest;
import io.fabric8.kubernetes.api.model.admission.AdmissionResponseBuilder;
import io.fabric8.kubernetes.api.model.admission.AdmissionReview;
import io.fabric8.kubernetes.api.model.admission.AdmissionReviewBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.*;
import java.io.StringReader;
import java.util.Base64;

import static javax.json.bind.JsonbConfig.FORMATTING;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

@Path("/pvc/mutate")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class MutatingAdmissionController {

    private static final Logger log = LoggerFactory.getLogger(MutatingAdmissionController.class);

    private final QuantityDeserializeFixer quantityDeserializeFixer;

    @ConfigProperty(name = "vsphere-extensions.readwritemany-sc")
    String storageClassName;

    @Inject
    public MutatingAdmissionController(QuantityDeserializeFixer quantityDeserializeFixer) {
        this.quantityDeserializeFixer = quantityDeserializeFixer;
    }

    @POST
    public AdmissionReview validate(AdmissionReview review) {
        quantityDeserializeFixer.fixDeserializeError(review);

        Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().setProperty(FORMATTING, true));
        log.info("received admission review: {}", jsonb.toJson(review));

        AdmissionRequest request = review.getRequest();
        AdmissionResponseBuilder responseBuilder = new AdmissionResponseBuilder()
                .withAllowed(true)
                .withUid(request.getUid());

        if("CREATE".equals(request.getOperation()) && request.getObject() instanceof PersistentVolumeClaim) {
            PersistentVolumeClaim object = (PersistentVolumeClaim) request.getObject();
            if(object.getSpec().getAccessModes().contains("ReadWriteMany")){

                JsonObject original = toJsonObject(object);
                object.getSpec().setStorageClassName(storageClassName);
                JsonObject mutated = toJsonObject(object);

                String patch = Json.createDiff(original, mutated).toString();
                String encoded = Base64.getEncoder().encodeToString(patch.getBytes());
                log.info("patching with {}", patch);

                responseBuilder
                        .withNewPatchType("JSONPatch")
                        .withPatch(encoded);

            }
        }

        AdmissionReview admissionReview = new AdmissionReviewBuilder().withResponse(responseBuilder.build()).build();
        //can't fix apiversion in kubernetes 1.17 it'll change it back v1, even though we apply v1beta1...
        admissionReview.setApiVersion("admission.k8s.io/v1");
        return admissionReview;
    }

    private JsonObject toJsonObject(HasMetadata object) {
        return Json.createReader(new StringReader(JsonbBuilder.create().toJson(object))).readObject();
    }

}