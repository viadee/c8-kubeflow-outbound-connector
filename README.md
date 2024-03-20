# Kubeflow Connector

A custom Camunda 8 outbound connector to communicate with the [Kubeflow](https://www.kubeflow.org/) API. It supports standalone as well as multi-suer setup and version 1 and 2 of the Kubeflow API. It currently supports the functions mentioned below in the [API section](#api-section).

# Build

You can package the Connector by running the following command:

```bash
mvn clean package
```

This will create the following artifacts:

- A thin JAR without dependencies.
- An fat JAR containing all dependencies, potentially shaded to avoid classpath conflicts. This will not include the SDK artifacts since those are in scope `provided` and will be brought along by the respective Connector Runtime executing the Connector.

# <a id="api-section"></a> API
Currenty this connector supports the following methods from the Kubeflow Pipeline API in both API versions 1 and 2.

- Get Pipelines
- Get Experiments
- Get Experiment by ID
- Get Runs
- Get Run by ID
- Get Run by Name
- Start Run
- Start Run and Monitor
- Create Experiment

The inputs describes the parameters that can be set in the modeler for the operation.
The output is the complete output you will get written into the variable you enter under Output mapping in the result variable. In the result expression you can more specifically extract the data as required to limit the output.

## Get Pipelines

### Input

#### Parameter Filter (optional)

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

#### Parameter Namespace (optional)

API Version 1 and 2: If omitted, shared pipelines will also be returned.

### Output

API Version 1: [https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiListPipelinesResponse](https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiListPipelinesResponse)

API Version 2: [https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#operation--apis-v2beta1-pipelines-get](https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#operation--apis-v2beta1-pipelines-get)

## Get Experiments

### Input

#### Parameter Filter

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

#### Parameter Namespace (required in multiuser deployment)

API Version 1 and 2: namespace where the experiments should be retrieved from.

### Output

API Version 1: [https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiListExperimentsResponse](https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiListExperimentsResponse)

API Version 2: [https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1ListExperimentsResponse](https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1ListExperimentsResponse)

## Get Experiment By ID

### Input

#### Parameter Experiment ID
The ID of the experiment that should be retrieved. 

### Output

API Version 1: [https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiExperiment](https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiExperiment)

API Version 2: [https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1Experiment](https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1Experiment)

## <a id="get-runs"></a> Get Runs

### Input

#### Parameter Filter

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

#### Parameter Namespace (required in multiuser deployment)

API Version 1 and 2: namespace where the runs should be retrieved from.

### Output

API Version 1: [https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiListRunsResponse](https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiListRunsResponse)

API Version 2: [https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1ListRunsResponse](https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1ListRunsResponse)


## Get Run By ID

### Input

#### Parameter Run ID
The ID of the run that should be retrieved. 

### Output

API Version 1: [https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiRunDetail](https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiRunDetail)

API Version 2: [https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1Run](https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1Run)

## Get Run By Name
This request tries to retrieve a single run by looking for a run that contains the name defined in the name parameter.

### Input

#### Parameter Name
The name to look for in the runs. This looks for the name as a substring not as equal.

### Output
equal to responses of [Get Runs](#get-runs)


## <a id="start-run"></a> Start Run
This operation starts a run and continues the process without waiting for the pipeline to finish.

### Input

#### Parameter Pipeline ID
The ID of the pipeline that should be run.

#### Parameter Experiment ID
The ID of the experiment in which the run should be started.

### Output

API Version 1: [https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiRunDetail](https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiRunDetail)

API Version 2: [https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1Run](https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1Run)

## Start Run and Monitor
This operation starts a run and monitors it until it finishes are throws an error. If the connector crashes during the run, the connector will pick up the already started run and monitor it after receiving the job again from Zeebe.

### Input

#### Parameter Pipeline ID
The ID of the pipeline that should be run.

#### Parameter Experiment ID
The ID of the experiment in which the run should be started.

### Output
see [Start Run](#start-run)

## Create Experiment
This operation allows to create an experiment, which can e. g. be used in a subsequent run.

### Input

#### Parameter Experiment Name
The name of the experiment to create.

#### Parameter Experiment Description (optional)
The description of the experiment.

### Output

API Version 1: [https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiExperiment](https://www.kubeflow.org/docs/components/pipelines/v1/reference/api/kubeflow-pipeline-api-spec/#/definitions/apiExperiment)

API Version 2: [https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1Experiment](https://www.kubeflow.org/docs/components/pipelines/v2/reference/api/kubeflow-pipeline-api-spec/#/definitions/v2beta1Experiment)

## Error handling

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
