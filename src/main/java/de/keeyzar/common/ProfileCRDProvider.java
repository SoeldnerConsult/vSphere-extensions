package de.keeyzar.common;

import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProfileCRDProvider {
    private CustomResourceDefinitionContext profileCRDcontext;
    public ProfileCRDProvider(){
        CustomResourceDefinitionContext.Builder builder = new CustomResourceDefinitionContext.Builder();
        profileCRDcontext = builder.withGroup("kubeflow.org")
                .withScope("Cluster")
                .withKind("Profile")
                .withPlural("profiles")
                .withVersion("v1")
                .build();
    }

    public CustomResourceDefinitionContext getCRDContext(){
        return profileCRDcontext;
    }
}
