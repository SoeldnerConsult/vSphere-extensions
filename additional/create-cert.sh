#!/bin/zsh

mkdir -p target/cert
cp csr.json target/cert
pushd target/cert

# Create private key and CSR
cfssl genkey csr.json | cfssljson -bare vsphere-extensions

# Create CSR k8s object
cat <<EOF | kubectl create -f -
apiVersion: certificates.k8s.io/v1beta1
kind: CertificateSigningRequest
metadata:
  name: vsphere-extensions
spec:
  groups:
  - system:authenticated
  request: $(cat vsphere-extensions.csr | base64 | tr -d '\n')
  usages:
  - digital signature
  - key encipherment
  - server auth
EOF

# Approve certificate
kubectl certificate approve vsphere-extensions

sleep 5s

# Download public key
kubectl get csr vsphere-extensions -o jsonpath='{.status.certificate}' | base64 --decode > vsphere-extensions.crt

cp vsphere-extensions-key.pem tls.key
cp vsphere-extensions.crt tls.crt
kubectl create secret tls vsphere-extensions-tls -n vsphere-extensions --key ./tls.key --cert ./tls.crt

# Display public key content
openssl x509 -in tls.crt -text
  #Propriétaire : CN=vsphere-extensions.vsphere-extensions.svc
  #Emetteur : CN=kubernetes

popd
