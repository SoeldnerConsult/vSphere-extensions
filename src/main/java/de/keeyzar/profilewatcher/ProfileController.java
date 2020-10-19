package de.keeyzar.profilewatcher;

import de.keeyzar.common.ProfileCRDProvider;
import de.keeyzar.pojo.profile.Profile;
import de.keeyzar.pojo.profile.ProfileList;
import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ProfileController {
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private KubernetesClient kubernetesClient;
    private ProfileCRDProvider profileCRDProvider;

    public ProfileController(){}

    @Inject
    public ProfileController(KubernetesClient kubernetesClient, ProfileCRDProvider profileCRDProvider) {
        this.kubernetesClient = kubernetesClient;
        this.profileCRDProvider = profileCRDProvider;
    }

    public boolean doesProfileExist(String name){
        log.info("Searching profile: {}", name);
        MixedOperation<Profile, ProfileList, Doneable<Profile>, Resource<Profile, Doneable<Profile>>> profileOp =
                kubernetesClient.customResources(
                profileCRDProvider.getCRDContext(), Profile.class, ProfileList.class, null
        );

        Resource<Profile, Doneable<Profile>> profileResource = profileOp.withName(name);
        try {
            Profile profile = profileResource.fromServer().get();
            return profile != null;
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
