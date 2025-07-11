/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.inference.services.custom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.common.Strings;
import org.elasticsearch.inference.InferenceServiceResults;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.xpack.inference.external.http.retry.RequestSender;
import org.elasticsearch.xpack.inference.external.http.retry.ResponseHandler;
import org.elasticsearch.xpack.inference.external.http.sender.BaseRequestManager;
import org.elasticsearch.xpack.inference.external.http.sender.ChatCompletionInput;
import org.elasticsearch.xpack.inference.external.http.sender.EmbeddingsInput;
import org.elasticsearch.xpack.inference.external.http.sender.ExecutableInferenceRequest;
import org.elasticsearch.xpack.inference.external.http.sender.InferenceInputs;
import org.elasticsearch.xpack.inference.external.http.sender.QueryAndDocsInputs;
import org.elasticsearch.xpack.inference.services.custom.request.CompletionParameters;
import org.elasticsearch.xpack.inference.services.custom.request.CustomRequest;
import org.elasticsearch.xpack.inference.services.custom.request.EmbeddingParameters;
import org.elasticsearch.xpack.inference.services.custom.request.RequestParameters;
import org.elasticsearch.xpack.inference.services.custom.request.RerankParameters;
import org.elasticsearch.xpack.inference.services.custom.response.CustomResponseEntity;

import java.util.Objects;
import java.util.function.Supplier;

public class CustomRequestManager extends BaseRequestManager {
    private static final Logger logger = LogManager.getLogger(CustomRequestManager.class);

    record RateLimitGrouping(int apiKeyHash) {
        public static RateLimitGrouping of(CustomModel model) {
            Objects.requireNonNull(model);

            return new RateLimitGrouping(model.rateLimitServiceSettings().hashCode());
        }
    }

    private static ResponseHandler createCustomHandler() {
        return new CustomResponseHandler("custom model", CustomResponseEntity::fromResponse);
    }

    public static CustomRequestManager of(CustomModel model, ThreadPool threadPool) {
        return new CustomRequestManager(Objects.requireNonNull(model), Objects.requireNonNull(threadPool));
    }

    private final CustomModel model;
    private final ResponseHandler handler;

    private CustomRequestManager(CustomModel model, ThreadPool threadPool) {
        super(threadPool, model.getInferenceEntityId(), RateLimitGrouping.of(model), model.rateLimitServiceSettings().rateLimitSettings());
        this.model = model;
        this.handler = createCustomHandler();
    }

    @Override
    public void execute(
        InferenceInputs inferenceInputs,
        RequestSender requestSender,
        Supplier<Boolean> hasRequestCompletedFunction,
        ActionListener<InferenceServiceResults> listener
    ) {
        RequestParameters requestParameters;
        if (inferenceInputs instanceof QueryAndDocsInputs) {
            requestParameters = RerankParameters.of(QueryAndDocsInputs.of(inferenceInputs));
        } else if (inferenceInputs instanceof ChatCompletionInput chatInputs) {
            requestParameters = CompletionParameters.of(chatInputs);
        } else if (inferenceInputs instanceof EmbeddingsInput) {
            requestParameters = EmbeddingParameters.of(
                EmbeddingsInput.of(inferenceInputs),
                model.getServiceSettings().getInputTypeTranslator()
            );
        } else {
            listener.onFailure(
                new ElasticsearchStatusException(
                    Strings.format("Invalid input received from custom service %s", inferenceInputs.getClass().getSimpleName()),
                    RestStatus.BAD_REQUEST
                )
            );
            return;
        }

        try {
            var request = new CustomRequest(requestParameters, model);
            execute(new ExecutableInferenceRequest(requestSender, logger, request, handler, hasRequestCompletedFunction, listener));
        } catch (Exception e) {
            // Intentionally not logging this exception because it could contain sensitive information from the CustomRequest construction
            listener.onFailure(
                new ElasticsearchStatusException("Failed to construct the custom service request", RestStatus.BAD_REQUEST, e)
            );
        }
    }
}
