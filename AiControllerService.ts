/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class AiControllerService {
    /**
     * stopGeneration
     * @param conversationId conversationId
     * @returns any OK
     * @throws ApiError
     */
    public static stopGenerationUsingGet(
        conversationId?: string,
    ): CancelablePromise<Record<string, any>> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/stop-generation',
            query: {
                'conversationId': conversationId,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * handleSse
     * @param conversationId conversationId
     * @param message message
     * @param model model
     * @returns any OK
     * @throws ApiError
     */
    public static handleSseUsingGet(
        conversationId?: string,
        message?: string,
        model?: string,
    ): CancelablePromise<any> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/stream',
            query: {
                'conversationId': conversationId,
                'message': message,
                'model': model,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
}
