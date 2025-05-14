package com.xin.xinoj.Aiagent.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI模型策略上下文类
 */
@Component
public class AiModelContext {
    private static final Logger logger = LoggerFactory.getLogger(AiModelContext.class);
    
    private final Map<String, AiModelStrategy> strategies = new HashMap<>();
    private final Map<String, String> requestIdToModelMap = new HashMap<>();
    private AiModelStrategy defaultStrategy;
    
    // 注入所有实现了AiModelStrategy接口的Bean
    @Autowired
    private List<AiModelStrategy> strategyList;
    
    // 在构造函数后自动调用，初始化策略映射
    @PostConstruct
    public void init() {
        // 注册所有策略实现
        for (AiModelStrategy strategy : strategyList) {
            registerStrategy(strategy);
        }
        
        // 如果没有找到默认策略，使用第一个作为默认
        if (defaultStrategy == null && !strategies.isEmpty()) {
            defaultStrategy = strategies.values().iterator().next();
        }
    }
    
    /**
     * 注册策略实现
     * @param strategy 策略实现
     */
    private void registerStrategy(AiModelStrategy strategy) {
        Class<?> strategyClass = strategy.getClass();
        
        // 获取策略上的AiModel注解
        if (strategyClass.isAnnotationPresent(AiModel.class)) {
            AiModel aiModel = strategyClass.getAnnotation(AiModel.class);
            String modelCode = aiModel.value();
            
            // 注册策略
            strategies.put(modelCode, strategy);
            
            // 如果是默认策略，设置默认策略
            if (aiModel.isDefault()) {
                defaultStrategy = strategy;
            }
        }
    }
    
    /**
     * 处理AI请求
     * @param model 模型名称
     * @param pw 输出响应的PrintWriter
     * @param message 用户消息
     * @throws InterruptedException 当处理被中断时
     */
    public void processRequest(String model, PrintWriter pw, String message,String conversationId) throws InterruptedException {
        // 查找对应策略或使用默认策略
        AiModelStrategy strategy = strategies.getOrDefault(model, defaultStrategy);
        if (strategy != null) {
            strategy.processAiRequest(pw, message,conversationId);
        } else {
            // 如果没有找到对应的策略和默认策略，抛出异常
            throw new IllegalArgumentException("未找到匹配的AI模型策略: " + model);
        }
    }
    
    /**
     * 停止内容生成
     * @param requestId 请求ID
     * @return 是否成功停止
     */
    public boolean stopGeneration(String requestId) {
        logger.info("尝试停止生成，请求ID: {}", requestId);
        // 处理前端传来的非标准格式的请求ID
        if (requestId.startsWith("req_")) {
            logger.info("收到非标准格式的请求ID，将尝试所有策略");
            // 尝试使用所有策略停止生成
            for (AiModelStrategy strategy : strategyList) {
                boolean result = strategy.stopGeneration(requestId);
                if (result) {
                    logger.info("成功使用策略 {} 停止生成", strategy.getClass().getSimpleName());
                    return true;
                }
            }
            return false;
        }
        
        // 标准格式请求ID处理
        String modelPrefix = extractModelPrefix(requestId);
        
        // 遍历所有策略尝试停止生成
        for (AiModelStrategy strategy : strategyList) {
            if (strategy.stopGeneration(requestId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 从请求ID提取模型前缀
     * @param requestId 请求ID
     * @return 模型前缀
     */
    private String extractModelPrefix(String requestId) {
        if (requestId == null || requestId.isEmpty()) {
            return "";
        }
        
        int dashIndex = requestId.indexOf("-");
        if (dashIndex > 0) {
            return requestId.substring(0, dashIndex);
        }
        
        return "";
    }
} 