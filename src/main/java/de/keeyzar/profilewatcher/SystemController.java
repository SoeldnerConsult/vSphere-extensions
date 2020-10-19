package de.keeyzar.profilewatcher;


import io.fabric8.kubernetes.api.model.Namespace;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class SystemController {
    private static final Logger log = LoggerFactory.getLogger(SystemController.class);

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
        log.info("initialize listening");
        namespaceController.listenToAddedNamespaceEvent(this::handleAddedNamespaceEvent);
    }

    private void handleAddedNamespaceEvent(Namespace namespace) {
        log.info("found namespace with {}", namespace.getMetadata().getName());
        if(isRoleBindingNecessary(namespace)){
            log.info("we determined a RoleBinding is necessary");
            roleBindingController.createPrivilegedRoleBindingForNamespace(namespace.getMetadata().getName());
        } else {
            log.info("no RoleBinding was necessary");
        }
    }

    private boolean isRoleBindingNecessary(Namespace namespace) {
        String namespace_name = namespace.getMetadata().getName();
        return profileController.doesProfileExist(namespace_name) &&
                !roleBindingController.doesRoleBindingExists(namespace_name);
    }


}
