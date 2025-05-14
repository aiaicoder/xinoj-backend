package com.xin.xinoj.Aiagent.strategy;


import com.xin.xinoj.model.dto.Aiagent.AiResult;
import com.xin.xinoj.model.dto.Aiagent.ContentDto;
import com.xin.xinoj.utils.JsonUtils;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Ultra模型策略实现
 */
@Component
@AiModel("4.0Ultra")
public class UltraModelStrategy implements AiModelStrategy {
    private static final Logger logger = LoggerFactory.getLogger(UltraModelStrategy.class);
    private static final String DONE = "[DONE]";
    private static final Integer timeout = 60;
    private static final String AI_URL = "https://spark-api-open.xf-yun.com/v1/chat/completions";
    
    // 存储活跃的EventSource连接，键为请求ID
    private final Map<String, EventSource> activeConnections = new ConcurrentHashMap<>();

    @Value("${api.password:}")
    private String apiPassword;

    @Override
    public void processAiRequest(PrintWriter pw, String content,String requestId) throws InterruptedException {

        
        Map<String, Object> params = new HashMap<>();
        params.put("model", "4.0Ultra");

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", content);

        List<Map> messages = new ArrayList<>();
        messages.add(message);
        params.put("messages", messages);
        params.put("stream", true);
        String jsonParams = JsonUtils.convertObj2Json(params);

        Request.Builder builder = new Request.Builder().url(AI_URL);
        builder.addHeader("Authorization", " Bearer " + apiPassword);
        builder.addHeader("Accept", "text/event-stream");
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = builder.post(body).build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).writeTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout,
                TimeUnit.SECONDS).build();

        // 实例化EventSource，注册EventSource监听器
        CountDownLatch eventLatch = new CountDownLatch(1);

        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                if (DONE.equals(data)) {
                    return;
                }
                String content = getContent(data);
                pw.write("data:" + JsonUtils.convertObj2Json(new ContentDto(content)) + "\n\n");
                pw.flush();
            }

            @Override
            public void onClosed(EventSource eventSource) {
                super.onClosed(eventSource);
                activeConnections.remove(requestId);
                eventLatch.countDown();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                logger.info("调用接口失败{}", t);
                activeConnections.remove(requestId);
                if (eventLatch != null) {
                    eventLatch.countDown();
                }
            }
        });
        
        // 存储连接用于稍后可能的取消操作
        activeConnections.put(requestId, realEventSource);
        
        
        
        // 与服务器建立连接
        realEventSource.connect(client);
        
        try {
            // await() 方法被调用来阻塞当前线程，直到 CountDownLatch 的计数变为0。
            eventLatch.await();
        } finally {
            // 确保连接从活跃列表中移除
            activeConnections.remove(requestId);
        }
    }
    
    @Override
    public boolean stopGeneration(String requestId) {
        EventSource eventSource = activeConnections.get(requestId);
        if (eventSource != null) {
            logger.info("停止Ultra模型的内容生成，请求ID: {}", requestId);
            eventSource.cancel();
            activeConnections.remove(requestId);
            return true;
        }
        logger.info("未找到Ultra模型对应的请求ID: {}", requestId);
        return false;
    }

    private String getContent(String data) {
        AiResult aiResult = JsonUtils.convertJson2Obj(data, AiResult.class);
        return aiResult.getChoices().get(0).getDelta().getContent();
    }

} 