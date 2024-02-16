package de.viadee.bpm.camunda.connectors.kubeflow.services;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.viadee.bpm.camunda.connectors.kubeflow.entities.KubeflowConnectorRequest;
import de.viadee.bpm.camunda.connectors.kubeflow.entities.input.KubeflowApi;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApiOperationsEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.KubeflowApisEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.enums.V1PipelineRunStatusEnum;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.ExecutionHandler;
import de.viadee.bpm.camunda.connectors.kubeflow.services.async.KubeflowCallable;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.JsonHelper;
import de.viadee.bpm.camunda.connectors.kubeflow.utils.RunUtil;
import io.swagger.client.model.V1ApiParameter;
import io.swagger.client.model.V1ApiPipelineSpec;
import io.swagger.client.model.V1ApiResourceKey;
import io.swagger.client.model.V1ApiResourceReference;
import io.swagger.client.model.V1ApiResourceType;
import io.swagger.client.model.V1ApiRun;
import io.swagger.client.model.V2beta1PipelineVersionReference;
import io.swagger.client.model.V2beta1Run;
import io.swagger.client.model.V2beta1RuntimeConfig;
import io.swagger.client.model.V2beta1RuntimeState;

public class KubeflowConnectorExecutorStartRun extends KubeflowConnectorExecutor {

	private static final List<String> RUN_STATUS_LIST_V1 = List.of(
			V1PipelineRunStatusEnum.SUCCEEDED.getValue(),
			V1PipelineRunStatusEnum.FAILED.getValue(),
			V1PipelineRunStatusEnum.SKIPPED.getValue(),
			V1PipelineRunStatusEnum.ERROR.getValue());

	private static final List<String> RUN_STATUS_LIST_V2 = List.of(
			V2beta1RuntimeState.SUCCEEDED.getValue(),
			V2beta1RuntimeState.FAILED.getValue(),
			V2beta1RuntimeState.SKIPPED.getValue(),
			V2beta1RuntimeState.CANCELED.getValue());

	private RunUtil runUtil;
	private String runName;

	public KubeflowConnectorExecutorStartRun(KubeflowConnectorRequest connectorRequest, long processInstanceKey,
			KubeflowApisEnum kubeflowApisEnum,
			KubeflowApiOperationsEnum kubeflowApiOperationsEnum) {
		super(connectorRequest, processInstanceKey, kubeflowApisEnum, kubeflowApiOperationsEnum);
		runUtil = new RunUtil();
	}

	@Override
	protected BodyPublisher buildPayloadForKubeflowEndpoint() {
		//define runName
		runName = Long.toString(processInstanceKey)+"_"+connectorRequest.getKubeflowapi().runName();
		
		try {
			if (KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)) {
				return HttpRequest.BodyPublishers
						.ofString(JsonHelper.objectMapper.writeValueAsString(getPayloadForEndpointV1()));
			}
			return HttpRequest.BodyPublishers
					.ofString(JsonHelper.objectMapper.writeValueAsString(getPayloadForEndpointV2()));

		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public HttpResponse<String> execute() {
		HttpResponse<String> result = null;
		if (kubeflowApiOperationsEnum.equals(KubeflowApiOperationsEnum.START_RUN)) {
			result = super.execute();
		} else if (kubeflowApiOperationsEnum.equals(KubeflowApiOperationsEnum.START_RUN_AND_MONITOR)) {
			String idOfAlreadyStartedRun;
			try {
				idOfAlreadyStartedRun = getIdOfAlreadyStartedRunByName(this.runName);
			} catch (InstantiationException | IllegalAccessException | IOException e) {
				throw new RuntimeException(e);
			}
			final Duration pollingInterval = Duration
					.parse(connectorRequest.getKubeflowapi().pollingInterval());

			if (idOfAlreadyStartedRun == null) { // run not yet started
				result = super.execute();
				String newRunId;
				try {
					newRunId = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)
							? runUtil.extractIdFromV1RunResponse(result)
							: runUtil.extractIdFromV2RunResponse(result);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
				KubeflowCallable kubeflowCallableRunNotStarted = new KubeflowCallable(connectorRequest,
						processInstanceKey, newRunId);
				result = retrieveRunStatusWithDelay(kubeflowCallableRunNotStarted,
						pollingInterval.getSeconds(),
						false);

			} else { // run already started
				KubeflowCallable kubeflowCallableRunStarted = new KubeflowCallable(connectorRequest, processInstanceKey,
						idOfAlreadyStartedRun);
				result = retrieveRunStatusWithDelay(kubeflowCallableRunStarted,
						pollingInterval.getSeconds(),
						true);
			}

		} else {
			throw new RuntimeException(
					"Unknown kubeflow operation: " + connectorRequest.getKubeflowapi().operation());
		}

		return result;
	}

	private V1ApiRun getPayloadForEndpointV1() {
		Map<String , Object> runParameters = connectorRequest.getKubeflowapi().runParameters();
		List<V1ApiParameter> v1ApiParameters = RunUtil.convertToV1ApiParameterList(runParameters);

		var v1ApiPipelineSpec = new V1ApiPipelineSpec()
				.pipelineId(connectorRequest.getKubeflowapi().pipelineId())
				.parameters(v1ApiParameters);

		var v1ApiResourceReference = new V1ApiResourceReference()
				.key(new V1ApiResourceKey()
						.type(V1ApiResourceType.EXPERIMENT)
						.id(connectorRequest.getKubeflowapi().experimentId()));

		var v1ApiRun = new V1ApiRun()
				.name(this.runName)
				.pipelineSpec(v1ApiPipelineSpec)
				.addResourceReferencesItem(v1ApiResourceReference);

		return v1ApiRun;
	}

	private V2beta1Run getPayloadForEndpointV2() {
		var v2beta1PipelineVersionReference = new V2beta1PipelineVersionReference()
				.pipelineId(connectorRequest.getKubeflowapi().pipelineId());

		var v2beta1RuntimeConfig = new V2beta1RuntimeConfig()
				.parameters(connectorRequest.getKubeflowapi().runParameters());

		var v2ApiRun = new V2beta1Run()
				.displayName(this.runName)
				.runtimeConfig(v2beta1RuntimeConfig)
				.pipelineVersionReference(v2beta1PipelineVersionReference)
				.experimentId(connectorRequest.getKubeflowapi().experimentId());

		return v2ApiRun;
	}

	private String getIdOfAlreadyStartedRunByName(String runName)
			throws InstantiationException, IllegalAccessException, IOException {
		KubeflowApi kubeflowApi = new KubeflowApi(kubeflowApisEnum.getValue(),
				KubeflowApiOperationsEnum.GET_RUN_BY_NAME.getValue(), null,
				null, null, null, null, runName, null, null, null, connectorRequest.getKubeflowapi()
				.httpHeaders());

		KubeflowConnectorRequest getRunByNameConnectorRequest = new KubeflowConnectorRequest(
				connectorRequest.getAuthentication(),
				connectorRequest.getConfiguration(), kubeflowApi,
				connectorRequest.getConnectionTimeoutInSeconds());

		KubeflowConnectorExecutorGetRunByName getRunByNameExecutor = (KubeflowConnectorExecutorGetRunByName) ExecutionHandler
				.getExecutor(
						getRunByNameConnectorRequest,
						processInstanceKey);
		String id = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)
				? getRunByNameExecutor.getRunByNameV1Typed().map(V1ApiRun::getId).orElse(null)
				: getRunByNameExecutor.getRunByNameV2Typed().map(V2beta1Run::getRunId)
						.orElse(null);

		return id;
	}

	private HttpResponse<String> retrieveRunStatusWithDelay(KubeflowCallable kubeflowCallable, long delay,
			boolean isPerformPreCheck) {
		HttpResponse<String> httpResponse = null;
		String status = "";
		var statusListToCheckAgainst = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)
				? RUN_STATUS_LIST_V1
				: RUN_STATUS_LIST_V2;
		while (!statusListToCheckAgainst.contains(status)) {
			try {
				if (isPerformPreCheck) {
					httpResponse = ExecutionHandler.runCallableAfterDelay(kubeflowCallable, 0,
							TimeUnit.SECONDS);
					isPerformPreCheck = false;
				} else {
					httpResponse = ExecutionHandler.runCallableAfterDelay(kubeflowCallable, delay,
							TimeUnit.SECONDS);
				}
				status = KubeflowApisEnum.PIPELINES_V1.equals(kubeflowApisEnum)
						? runUtil.readV1RunAsTypedResponse(httpResponse).getStatus()
						: runUtil.readV2RunAsTypedResponse(httpResponse).getState().getValue();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		return httpResponse;
	}
}
