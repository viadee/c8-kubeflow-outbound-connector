# Kubeflow Connector

A custom Camunda 8 outbound connector to communicate with the [Kubeflow](https://www.kubeflow.org/) API. It supports standalone as well as multi-suer setup and version 1 and 2 of the Kubeflow API. It currently supports the functions mentioned below in the [API section](#api-section).

## Build

You can package the Connector by running the following command:

```bash
mvn clean package
```

This will create the following artifacts:

- A thin JAR without dependencies.
- An fat JAR containing all dependencies, potentially shaded to avoid classpath conflicts. This will not include the SDK artifacts since those are in scope `provided` and will be brought along by the respective Connector Runtime executing the Connector.

## <a id="api-section"></a> API
Currenty this connector supports the following methods from the Kubeflow Pipeline API in both API versions 1 and 2.

- Get Pipelines
- Get Experiments
- Get Runs
- Get Run by ID
- Get Run by Name
- Start Run
- Start Run and Monitor
- Create Experiment

The inputs describes the parameters that can be set in the modeler for the operation.
The output is the complete output you will get written into the variable you enter under Output mapping in the result variable. In the result expression you can more specifically extract the data as required to limit the output.

### Get Pipelines

#### Input

Parameter Filter

API Version 1:

```json
{ "predicates":
  [
    {
      "op":"IS_SUBSTRING",
      "key": "name",
      "string_value": "Control"
    }
  ]
}
```

API Version 2:
```json
{ "predicates":
  [
    {
      "operation":"IS_SUBSTRING",
      "key": "name",
      "string_value": "Control"
    }
  ]
}
```
For details on filter structure and options check the proto-buffer files:

Version 1: [https://github.com/kubeflow/pipelines/blob/master/backend/api/v1beta1/filter.proto](https://github.com/kubeflow/pipelines/blob/master/backend/api/v1beta1/filter.proto)

Version 2: [https://github.com/kubeflow/pipelines/blob/master/backend/api/v2beta1/filter.proto](https://github.com/kubeflow/pipelines/blob/master/backend/api/v2beta1/filter.proto)

#### Output

API Version 1:
```json
{
  "pipelines":[
    {
      "id":"2d974767-3d50-40b8-9bfc-7767d4a13a5c",
      "created_at":"2024-03-15T10:43:45Z",
      "name":"[Tutorial] DSL - Control structures",
      "description":"...",
      "default_version":{
        "id":"dadc4c6d-b864-49e6-808e-946424e532ba",
        "name":"[Tutorial] DSL - Control structures",
        "created_at":"2024-03-15T10:43:45Z",
        "resource_references":[
          {
            "key":{
              "type":"PIPELINE",
              "id":"2d974767-3d50-40b8-9bfc-7767d4a13a5c"
            },
            "relationship":"OWNER"
          }
        ],
        "description":"..."
      }
    }
  ],
  "total_size":1
}
```

API Version 2:
```json
{
  "pipelines": [
    {
      "pipeline_id":"369eea92-e69c-4af4-802a-9f403a362bc5",
      "display_name":"[Tutorial] DSL - Control structures",
      "description":"...",
      "created_at":"2024-03-14T22:09:25Z"
    }
  ],
  "total_size":1
}
```

### Get Experiments

#### Input

Parameter Filter

API Version 1:
```json
{ "predicates":
  [
    {
      "op":"IS_SUBSTRING",
      "key": "name",
      "string_value": "test"
    }
  ]
}
```

API Version 2:
```json
{ "predicates":
  [
    {
      "operation":"IS_SUBSTRING",
      "key": "name",
      "string_value": "test"
    }
  ]
}
```

#### Output

API Version 1:
```json
{
  "experiments":[
    {
      "id":"f4e404a0-6b83-44a8-9eae-9678706188a0",
      "name":"Testexperiment",
      "created_at":"2024-03-15T11:47:52Z",
      "resource_references":[
        {
          "key":{
            "type":"NAMESPACE",
            "id":"kubeflow-user-example-com"
          },
          "relationship":"OWNER"
        }
      ],
      "storage_state":"STORAGESTATE_AVAILABLE"
    }
  ],
  "total_size":1
}
```

API Version 2:
```json
{
  "experiments": [
    {
      "experiment_id": "f4e404a0-6b83-44a8-9eae-9678706188a0",
      "display_name": "Testexperiment",
      "created_at": "2024-03-15T11:47:52Z",
      "namespace": "kubeflow-user-example-com",
      "storage_state": "AVAILABLE"
    }
  ],
  "total_size": 1
}
```

### Error handling

In case a return code >= 400 is returned by the API calls, the connector will raise an exception.

## Test locally

Run unit tests

```bash
mvn clean verify
```

### Test with local runtime

Use the [Camunda Connector Runtime](https://github.com/camunda-community-hub/spring-zeebe/tree/master/connector-runtime#building-connector-runtime-bundles) to run your function as a local Java application.

In your IDE you can also simply navigate to the `LocalContainerRuntime` class in test scope and run it via your IDE.
If necessary, you can adjust `application.properties` in test scope.

## Element Template
The element templates can be found in the [kubeflow-connector.json](./element-templates/kubeflow-connector.json) file.

# Local development environment setup

Start a local kubernets cluster e.g. with mimikube or kind.

Run the following command from the root folder in order to deploy the dev environment into the kubernetes cluster.
```bash
deployEnv.sh
```
This will deploy the relevant Kubeflow components (e.g. Pipelines) and a Keycloak server used for authentication and check if the NodePort is reachable on localhost and if not create a port-forward to the local port 30000.

If the NodePorts are not directly exposed on your local machine, you can create a port-forward manually to reach it using the following command.
```bash
kubectl port-forward -n istio-system svc/istio-ingressgateway 30000:80    
```

## Credentials used in the dev test environment
### Keycloak admin
URL: [http://localhost:30000/auth/](http://localhost:30000/auth/)

``` 
username: admin
password: admin
```

### User for loging into the Kubeflow UI
URL: [http://localhost:30000/](http://localhost:30000/)
``` 
username: user@example.com
password: 12341234
```

### Client credentials for authenticating against the Kubeflow API
The following credentials can be used during a client-credentials oauth flow to authenticate against Kubeflow
``` 
client-id: kubeflow
client-secret: Jq09L1liFa0UiaXnL3pcnXzlqOKXaoOW
```

## Contact Information
For any queries and further support, please drop us a mail at [camunda8-connector-su-aaaamkuzw6jcci2hm7rscvue7y@viadee.slack.com](mailto:camunda8-connector-su-aaaamkuzw6jcci2hm7rscvue7y@viadee.slack.com)

# OLD ==========

### Shading dependencies

You can use the `maven-shade-plugin` defined in the [Maven configuration](./pom.xml) to relocate common dependencies
that are used in other Connectors and the [Connector Runtime](https://github.com/camunda-community-hub/spring-zeebe/tree/master/connector-runtime#building-connector-runtime-bundles).
This helps to avoid classpath conflicts when the Connector is executed. 

Use the `relocations` configuration in the Maven Shade plugin to define the dependencies that should be shaded.
The [Maven Shade documentation](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html) 
provides more details on relocations.
