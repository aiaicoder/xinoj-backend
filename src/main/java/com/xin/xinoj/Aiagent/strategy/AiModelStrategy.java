package com.xin.xinoj.Aiagent.strategy;

import java.io.PrintWriter;

/**
 * AI模型处理策略接口
 * @author 15712
 */
public interface AiModelStrategy {
    /**
     * 处理AI请求
     * @param pw 输出响应的PrintWriter
     * @param message 用户消息
     * @throws InterruptedException 当处理被中断时
     */
    void processAiRequest(PrintWriter pw, String message,String requestId) throws InterruptedException;
    
    /**
     * 停止内容生成
     * @param requestId 请求ID，用于标识要停止的生成过程
     * @return 是否成功停止
     */
    boolean stopGeneration(String requestId);
} 