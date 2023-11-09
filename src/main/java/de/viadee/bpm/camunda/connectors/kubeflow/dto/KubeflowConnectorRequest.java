package de.viadee.bpm.camunda.connectors.kubeflow.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.Authentication;
import de.viadee.bpm.camunda.connectors.kubeflow.dto.input.KubeflowApi;
import io.camunda.connector.http.base.model.HttpCommonRequest;
import io.camunda.connector.http.base.model.HttpMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record KubeflowConnectorRequest (
    @Valid @NotNull Authentication authentication,
    @Valid @NotNull KubeflowApi kubeflowapi) {

        public HttpCommonRequest getHttpRequest() {
            String filter = kubeflowapi().filter().replaceAll("[\\\\r]?\\\\n", "").replace("\\\"", "\"");
            String encodedFilter = "";
            try {
                encodedFilter = "&filter=" + URLEncoder.encode(filter, "UTF-8").replace("+",
                        "%20");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String url = authentication().kubeflowUrl()
            + "/pipeline/apis/v1beta1/runs?resource_reference_key.type=NAMESPACE&resource_reference_key.id=kubeflow-user-example-com"
            + encodedFilter;

            HttpCommonRequest httpRequest = new HttpCommonRequest();
            httpRequest.setHeaders(Map.of("Cookie", "authservice_session=" + authentication().cookievalue()));
            httpRequest.setMethod(HttpMethod.GET);
            httpRequest.setUrl(url);

            return httpRequest;
        }
    }
