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

## Contact Information
For any queries and further support, please drop us a mail at [???](mailto:???)


# OLD ==========

### Shading dependencies

You can use the `maven-shade-plugin` defined in the [Maven configuration](./pom.xml) to relocate common dependencies
that are used in other Connectors and the [Connector Runtime](https://github.com/camunda-community-hub/spring-zeebe/tree/master/connector-runtime#building-connector-runtime-bundles).
This helps to avoid classpath conflicts when the Connector is executed. 

Use the `relocations` configuration in the Maven Shade plugin to define the dependencies that should be shaded.
The [Maven Shade documentation](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html) 
provides more details on relocations.
