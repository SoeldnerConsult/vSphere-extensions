package de.keeyzar.pojo.profile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

@JsonIgnoreProperties("status")
public class Profile extends CustomResource {

    @JsonProperty("apiVersion")
    private String apiVersion = "kubeflow.org/v1";

    @JsonProperty("kind")
    private String kind = "Profile";

    @JsonProperty()
    private ObjectMeta metadata;

    @JsonProperty()
    private ProfileSpec spec;

    public Profile() {
    }

    public Profile(String apiVersion, String kind, ObjectMeta metadata, ProfileSpec spec) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.spec = spec;
    }

    @Override
    public ObjectMeta getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public void setApiVersion(String version) {
        this.apiVersion = version;
    }

    @Override
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public ProfileSpec getSpec() {
        return spec;
    }

    public void setSpec(ProfileSpec spec) {
        this.spec = spec;
    }


    @Override
    public String toString() {
        return "Profile{" +
                "apiVersion='" + apiVersion + '\'' +
                ", kind='" + kind + '\'' +
                ", metadata=" + metadata +
                ", spec=" + spec +
                '}';
    }
}
