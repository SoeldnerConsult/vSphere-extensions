package de.keeyzar.namespaces;

import io.fabric8.kubernetes.api.model.Namespace;

public interface NamespaceAddedListener {
    void addedNamespaceEvent(Namespace addedNamespace);
}
