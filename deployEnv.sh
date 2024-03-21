#!/bin/bash
export KUBEFLOW_HOST=localhost
envsubst < ./src/test/resources/kubeflow-environment/oauth2_proxy.yml > ./src/test/resources/kubeflow-environment/oauth2_proxy2.yml 
mv ./src/test/resources/kubeflow-environment/oauth2_proxy2.yml ./src/test/resources/kubeflow-environment/oauth2_proxy.yml

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
  printf "\n\x1b[6;30;42mURLs:"
  printf "\nKubeflow UI: \x1b[6;37;42mhttp://localhost:30000/\x1b[6;30;42m"
  printf "\nKeycloak Admin UI: \x1b[6;37;42mhttp://localhost:30000/auth/\x1b[0m\n\n"
else
  printf "\n\nNodePort is not reachable. Starting port forward...\n\n"
  printf "\n\x1b[6;30;42mURLs:"
  printf "\nKubeflow UI: \x1b[6;37;42mhttp://localhost:30000/\x1b[6;30;42m"
  printf "\nKeycloak Admin UI: \x1b[6;37;42mhttp://localhost:30000/auth/\x1b[0m\n\n"
  printf "\n\n\x1b[6;30;42mKeep this terminal open! Press \x1b[6;37;42mCtrl+C \x1b[6;30;42mto stop the port-forward.\x1b[0m\n\n"
  kubectl port-forward -n istio-system svc/istio-ingressgateway 30000:80
fi

sed -i '' 's/'"$KUBEFLOW_HOST"':3000/${KUBEFLOW_HOST}:3000/g' ./src/test/resources/kubeflow-environment/oauth2_proxy.yml