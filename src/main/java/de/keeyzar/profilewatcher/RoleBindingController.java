package de.keeyzar.profilewatcher;

import de.keeyzar.common.NamingConvention;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.Subject;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RoleBindingController {
    private Logger LOGGER = Logger.getLogger(RoleBindingController.class.getName());

    private KubernetesClient kubernetesClient;
    public RoleBindingController() {
    }

    @Inject
    public RoleBindingController(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public boolean doesRoleBindingExists(String whichNamespace){
        String roleBindingName = NamingConvention.PSP_NAMING_PREFIX + whichNamespace;
        LOGGER.info(() -> String.format("Searching for existing Rolebinding: %s", roleBindingName));

        boolean doesExist = kubernetesClient.rbac().roleBindings()
                .inNamespace(whichNamespace)
                .withName(roleBindingName)
                .fromServer()
                .get() != null;

        LOGGER.info(() -> String.format("Did we found a RoleBinding with the name %s ? %b",
                roleBindingName, doesExist
                ));
        return doesExist;
    }

    public void createPrivilegedRoleBindingForNamespace(String whichNamespace){
        Subject subject = new SubjectBuilder()
                .withNewApiGroup("rbac.authorization.k8s.io")
                .withNewKind("Group")
                .withNewName("system:serviceaccounts:" + whichNamespace)
                .build();

        RoleBinding pspForServiceAccount = new RoleBindingBuilder()
                .withNewMetadata().withName(NamingConvention.PSP_NAMING_PREFIX + whichNamespace)
                .withNamespace(whichNamespace)
                .endMetadata()
                .withApiVersion("rbac.authorization.k8s.io/v1")
                .withNewRoleRef()
                .withNewKind("ClusterRole")
                .withNewName("psp:vmware-system-privileged")
                .withApiGroup("rbac.authorization.k8s.io")
                .endRoleRef()
                .withSubjects(subject)
                .build();

        retry(() -> {
            kubernetesClient.rbac().roleBindings().inNamespace(whichNamespace).create(pspForServiceAccount);
        });
    }

    private void retry(Runnable runnable){
        int maxRetries = 10;
        for(int counter = 0; counter <= maxRetries; counter++){
            try {
                runnable.run();
                break;
            } catch (Exception e){
                if(counter == maxRetries){
                    LOGGER.log(Level.SEVERE, e, () -> "Could not apply RoleBinding.. Stopping now");
                } else {
                    LOGGER.log(Level.INFO, e, () -> "Could not apply RoleBinding, retrying in some seconds");
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ex) {
                        LOGGER.info(() -> "Thread was interrupted..");
                    }
                }
            }
        }
    }
}
