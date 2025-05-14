package com.xin.xinoj.Aiagent.strategy;


import com.xin.xinoj.model.dto.Aiagent.ContentDto;
import com.xin.xinoj.model.dto.Aiagent.OllamaResult;
import com.xin.xinoj.utils.JsonUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Deepseek模型策略实现
 */
@Component
@AiModel(value = "deepseek-r1:8b", isDefault = true)
public class DeepseekModelStrategy implements AiModelStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DeepseekModelStrategy.class);
    private static final Integer timeout = 60;
    private static final String URL_OLLAMA = "http://localhost:11434/api/generate";
    private static final String MODEL_DEEPSEEK = "deepseek-r1:8b";
    
    // 存储活跃的请求，键为请求ID，值为取消标志
    private final Map<String, AtomicBoolean> activeRequests = new ConcurrentHashMap<>();

    @Override
    public void processAiRequest(PrintWriter pw, String message,String requestId) throws InterruptedException {
        // 生成唯一的请求ID
        AtomicBoolean shouldCancel = new AtomicBoolean(false);
        activeRequests.put(requestId, shouldCancel);

        Map<String, Object> params = new HashMap<>();
        params.put("prompt", message);
        params.put("model", MODEL_DEEPSEEK);
        params.put("stream", true);
        String jsonParams = JsonUtils.convertObj2Json(params);

        Request.Builder builder = new Request.Builder().url(URL_OLLAMA);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = builder.post(body).build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).writeTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout,
                TimeUnit.SECONDS).build();
        CountDownLatch eventLatch = new CountDownLatch(1);
        Call call = client.newCall(request);
        
        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("请求失败", e);
                    activeRequests.remove(requestId);
                    eventLatch.countDown();
                }
    
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            try (ResponseBody responseBody = response.body()) {
                                if (responseBody != null) {
                                    // 逐行读取响应
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8));
                                    String line;
                                    while ((line = reader.readLine()) != null && !shouldCancel.get()) {
                                        OllamaResult aiResult = JsonUtils.convertJson2Obj(line, OllamaResult.class);
                                        if (aiResult.getDone()) {
                                            break;
                                        }
                                        logger.info(aiResult.getResponse());
                                        pw.write("data:" + JsonUtils.convertObj2Json(new ContentDto(aiResult.getResponse())) + "\n\n");
                                        pw.flush();
                                    }
                                }
                            }
                        } else {
                            logger.error("请求失败", response);
                        }
                    } finally {
                        activeRequests.remove(requestId);
                        eventLatch.countDown();
                    }
                }
            });
            
            // await() 方法被调用来阻塞当前线程，直到 CountDownLatch 的计数变为0。
            eventLatch.await();
        } finally {
            // 确保请求从活跃列表中移除
            activeRequests.remove(requestId);
        }
    }
    
    @Override
    public boolean stopGeneration(String requestId) {
        AtomicBoolean shouldCancel = activeRequests.get(requestId);
        if (shouldCancel != null) {
            logger.info("停止Deepseek模型的内容生成，请求ID: {}", requestId);
            shouldCancel.set(true);
            return true;
        }
        logger.info("未找到Deepseek模型对应的请求ID: {}", requestId);
        return false;
    }

} 