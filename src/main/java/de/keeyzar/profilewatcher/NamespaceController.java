package de.keeyzar.profilewatcher;

import de.keeyzar.namespaces.NamespaceAddedListener;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class NamespaceController {

    private KubernetesClient kubernetesClient;

    public NamespaceController(){ }

    @Inject
    public NamespaceController(KubernetesClient kubernetesClient){
        this.kubernetesClient = kubernetesClient;
    }

    public void listenToAddedNamespaceEvent(NamespaceAddedListener namespaceAddedListener) {
        registerInformerListener(kubernetesClient, namespaceAddedListener);
    }

    private void registerInformerListener(KubernetesClient kubernetesClient, NamespaceAddedListener namespaceAddedListener) {
        SharedInformerFactory informers = kubernetesClient.informers();
        SharedIndexInformer<Namespace> namespaceSharedIndexInformer =
                informers.sharedIndexInformerFor(Namespace.class, NamespaceList.class, 1000);
        namespaceSharedIndexInformer.addEventHandler(new ResourceEventHandler<>() {
            @Override
            public void onAdd(Namespace addedNamespace) {
                namespaceAddedListener.addedNamespaceEvent(addedNamespace);
            }

            @Override
            public void onUpdate(Namespace oldObj, Namespace newObj) {
                //here we should do something..
            }

            @Override
            public void onDelete(Namespace obj, boolean deletedFinalStateUnknown) {
                //do Nothing, profile is deleted, because it is namespace bound
            }
        });
        informers.startAllRegisteredInformers();
    }
}
