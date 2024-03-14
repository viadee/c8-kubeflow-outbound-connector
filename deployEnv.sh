#!/bin/bash
printf "\n\nApplying resources...\n\n"
while ! kustomize build ./src/test/resources/kubeflow-environment | kubectl apply -f -; do
  printf "\n\nRetrying to apply resources...\n\n"
  sleep 10
done

printf "\n\nWaiting for all deployments to be up and running...\n\n"
kubectl wait --for=condition=available --timeout=300s deployment --all --all-namespaces
while ! kubectl get namespace kubeflow; do
  printf "\n\nStill waiting for all deployments to be up and running...\n\n"
  sleep 10
done

kubectl wait --for=condition=available --timeout=300s deployment --all --all-namespaces

printf "\n\nDeployment finished!\n\n"

printf "Checking if NodePort is reachable on localhost...\n\n"
if nc -v -z -w2 localhost 30000 &> /dev/null; then
  printf "\n\nNodePort is reachable!\n\n"
  printf "\nURLs:\n"
  printf "\nKubeflow UI: http://localhost:30000/"
  printf "\nKeycloak Admin UI: http://localhost:30000/auth/\n\n"
else
  printf "\n\nNodePort is not reachable. Starting port forward...\n\n"
  printf "\nURLs:\n"
  printf "\nKubeflow UI: http://localhost:30000/"
  printf "\nKeycloak Admin UI: http://localhost:30000/auth/\n\n"
  printf "\n\nKeep this terminal open! Press Ctrl+C to stop the port-forward.\n\n"
  kubectl port-forward -n istio-system svc/istio-ingressgateway 30000:80
fi