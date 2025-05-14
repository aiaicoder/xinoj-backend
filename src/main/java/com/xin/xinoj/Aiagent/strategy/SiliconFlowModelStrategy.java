package com.xin.xinoj.Aiagent.strategy;

import com.xin.xinoj.model.dto.Aiagent.AiResult;
import com.xin.xinoj.model.dto.Aiagent.ContentDto;
import com.xin.xinoj.utils.JsonUtils;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
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
 * SiliconFlow模型策略实现
 * 这是一个新添加的模型策略示例
 * @author 15712
 */
@Component
@AiModel("Pro/deepseek-ai/DeepSeek-V3")
public class SiliconFlowModelStrategy implements AiModelStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SiliconFlowModelStrategy.class);
    private static final String DONE = "[DONE]";
    private static final Integer timeout = 60;
    private static final String SILICON_FLOW = "https://api.siliconflow.cn/v1/chat/completions";
    
    // 存储活跃的EventSource连接，键为请求ID
    private final Map<String, EventSource> activeConnections = new ConcurrentHashMap<>();

    @Value("${api.ApiKey:}")
    private String apiKey;

    @Override
    public void processAiRequest(PrintWriter pw, String content,String requestId) throws InterruptedException {
        Map<String, Object> params = new HashMap<>();
        params.put("model", "Pro/deepseek-ai/DeepSeek-V3");

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", content);

        List<Map> messages = new ArrayList<>();
        messages.add(message);
        params.put("messages", messages);
        
        // 添加硅基流动API所需的其他参数
        params.put("stream", true);
        params.put("max_tokens", 512);
        params.put("temperature", 0.7);
        params.put("top_p", 0.7);
        params.put("top_k", 50);
        params.put("frequency_penalty", 0.5);
        params.put("n", 1);
        
        String jsonParams = JsonUtils.convertObj2Json(params);

        Request.Builder builder = new Request.Builder().url(SILICON_FLOW);
        builder.addHeader("Authorization", "Bearer " + apiKey);
        builder.addHeader("Content-Type", "application/json");
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = builder.post(body).build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).writeTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout,
                TimeUnit.SECONDS).build();

        
        
        // 实例化EventSource，注册EventSource监听器
        CountDownLatch eventLatch = new CountDownLatch(1);

        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, String id, String type, @NotNull String data) {
                if (DONE.equals(data)) {
                    return;
                }
                String content = getContent(data);
                pw.write("data:" + JsonUtils.convertObj2Json(new ContentDto(content)) + "\n\n");
                pw.flush();
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                super.onClosed(eventSource);
                activeConnections.remove(requestId);
                eventLatch.countDown();
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, Throwable t, Response response) {
                logger.info("调用接口失败{}", t);
                activeConnections.remove(requestId);
                eventLatch.countDown();
            }
        });
        
        // 存储连接用于稍后可能的取消操作
        activeConnections.put(requestId, realEventSource);
        
        try {
            // 与服务器建立连接
            realEventSource.connect(client);
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
            logger.info("停止硅基流动模型的内容生成，请求ID: {}", requestId);
            eventSource.cancel();
            activeConnections.remove(requestId);
            return true;
        }
        logger.info("未找到硅基流动模型对应的请求ID: {}", requestId);
        return false;
    }

    private String getContent(String data) {
        AiResult aiResult = JsonUtils.convertJson2Obj(data, AiResult.class);
        return aiResult.getChoices().get(0).getDelta().getContent();
    }
    

} 