# vsphere kubernetes extensions
1. fix Pod Security Policies for each freshly created Kubeflow namespace
2. fix PersistentVolumeClaim for ReadWriteMany claims to new k8s storage class

# 1. fix PSP
is done via namespace watching
new namespace? does corresponding profile exist? => kubeflow created namespace
-> create PSP for namespace

# 2. fix PVC
is done as an admission controller, because we can't modify claim after creation.
when read write many is requested use storage class provided by config map within k8s
- default storage class of vSphere cannot create RWMany SC; therefore an NFS Client must
be provided, with a corresponding storageClass, which will be utilized for each and
any read write many PVC
- this is done, because in TKG setting a SC is set via TanzuKubernetesCluster Resource
your new SC in this cluster is unknown to TKC resource in the supervisor cluster
therefore you can't set it there.
manually setting SC here will be overridden periodically.
Change utilized storage class in each kubeflow resource or 
write an admission controller, rewriting storage class for each read write many PVC

# installation
1. install initial resources (namespace...)
2. fix configmap properties (name of storage class for read write many)
3. package .jar ; create docker & Push docker file
4. install resources like rolebindings & app deployment
5. create certificate request, approve and finally push a webhook with
certificate of app deployment

```
kubectl apply -f additional/initial-resources.yaml

#setup configuration
readWriteManyStorageClass=nfs-client
resourcePath=additional/resources.yaml
sed -i "s/nfs-client/$readWriteManyStorageClass/" $resourcePath
sed "s/nfs-client/$readWriteManyStorageClass/" $resourcePath

#OPTIONAL: push docker image
mvn package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t keeyzar/vsphere-extensions-jvm .
docker push keeyzar/vsphere-extensions-jvm

#create certifcate request and sign..
#make sure, go; cfssl cfssljson are installed
pushd .
cd additional
chmod +x create-cert.sh
./create-cert.sh
popd 

#create all necessary resources
k apply -f $resourcePath

#deploy vsphere-extensions
controller=$(kubectl -n vsphere-extensions get pods --selector=app=vsphere-extensions -ojsonpath='{.items[*].metadata.name}')
kubectl -n vsphere-extensions wait --for=condition=Ready --timeout=300s pod/$controller

webhookResource=additional/webhook.yaml
cert=$(kubectl -n vsphere-extensions exec $controller -- cat /var/run/secrets/kubernetes.io/serviceaccount/ca.crt | base64 | tr -d '\n')
sed -i.bak -E "s/caBundle:.*?/caBundle: $cert/" $webhookResource
kubectl apply -f $webhookResource

#check functionality
k apply -f additional/test-pvc.yaml

#compare storageclasses
k get pvc -n default test-pvc -o=jsonpath="{.spec.storageClassName}"
echo $readWriteManyStorageClass
k delete pvc -n default test-pvc

#check logs
k logs -f $controller
```