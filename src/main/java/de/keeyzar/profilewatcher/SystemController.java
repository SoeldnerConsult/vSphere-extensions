package de.keeyzar.profilewatcher;


import io.fabric8.kubernetes.api.model.Namespace;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class SystemController {
    private Logger LOGGER = Logger.getLogger(SystemController.class.getName());

    private final NamespaceController namespaceController;
    private final ProfileController profileController;
    private final RoleBindingController roleBindingController;

    @Inject
    public SystemController(NamespaceController namespaceController, ProfileController profileController,
                            RoleBindingController roleBindingController){

        this.namespaceController = namespaceController;
        this.profileController = profileController;
        this.roleBindingController = roleBindingController;
    }

    void onStart(@Observes StartupEvent ev) {
        initializeNamespaceWatching();
    }


    private void initializeNamespaceWatching() {
        LOGGER.info(() -> "initialize listening");
        namespaceController.listenToAddedNamespaceEvent(this::handleAddedNamespaceEvent);
    }

    private void handleAddedNamespaceEvent(Namespace namespace) {
        LOGGER.info(() -> "found namespace with " + namespace.getMetadata().getName());
        if(isRoleBindingNecessary(namespace)){
            LOGGER.info(() -> "we determined a RoleBinding is necessary");
            roleBindingController.createPrivilegedRoleBindingForNamespace(namespace.getMetadata().getName());
        } else {
            LOGGER.info(() -> "no RoleBinding was necessary");
        }
    }

    private boolean isRoleBindingNecessary(Namespace namespace) {
        String namespace_name = namespace.getMetadata().getName();
        return profileController.doesProfileExist(namespace_name) &&
                !roleBindingController.doesRoleBindingExists(namespace_name);
    }


}
