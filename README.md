# Kubeflow Connector

A custom Camunda 8 outbound connector to communicate with the [Kubeflow](https://www.kubeflow.org/) API. It supports version 1 aqnd version 2 of the Kubeflow API and supports the functions mentioned below in the [API section](#api-section).

## Build

You can package the Connector by running the following command:

```bash
mvn clean package
```

This will create the following artifacts:

- A thin JAR without dependencies.
- An fat JAR containing all dependencies, potentially shaded to avoid classpath conflicts. This will not include the SDK artifacts since those are in scope `provided` and will be brought along by the respective Connector Runtime executing the Connector.

## <a id="api-section"></a> API
Currenty this connector supports the following methods from the Kubeflow Pipeline API.

- Get Pipelines
- Get Experiments
- Get Runs
- Get Run by ID
- Get Run by Name
- Start Run
- Start Run and Monitor
- Create Experiment

### Get Pipelines

### Input



| Name     | Description      | Example           | Notes                                                                      |
|----------|------------------|-------------------|----------------------------------------------------------------------------|
| filter | define the filter to apply to the call    | `alice`           | Has no effect on the function call outcome.                                |
| token    | Mock token value | `my-secret-token` | Has no effect on the function call outcome.                                |
| message  | Mock message     | `Hello World`     | Echoed back in the output. If starts with 'fail', an error will be thrown. |

### Output

```json
{
  "result": {
    "myProperty": "Message received: ..."
  }
}
```

### Error codes

| Code | Description                                |
|------|--------------------------------------------|
| FAIL | Message starts with 'fail' (ignoring case) |

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
