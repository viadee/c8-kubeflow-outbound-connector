#!/bin/bash
set -e

kustomize build $(dirname "$0") | kubectl apply -f -

echo "Waiting for all deployments to be ready"
kubectl wait --for=condition=available --timeout=600s deployment --all --all-namespaces
