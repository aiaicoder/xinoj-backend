package com.xin.xinoj.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xin.xinoj.Aiagent.strategy.AiModelContext;
import com.xin.xinoj.common.ErrorCode;
import com.xin.xinoj.constant.RedisKeyConstant;
import com.xin.xinoj.exception.BusinessException;
import com.xin.xinoj.manager.RedisLimiterManager;
import com.xin.xinoj.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 15712
 */
@RestController
public class AiController {
    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @Resource
    private AiModelContext aiModelContext;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private UserServiceImpl userService;

    @GetMapping(value = "/stream")
    public void handleSse(String model, String message, String conversationId, HttpServletResponse response) {
//        Long id = userService.getLoginUser().getId();
//        boolean b = redisLimiterManager.doRateLimitAI(RedisKeyConstant.REDIS_AI_KEY + id);
//        if (!b){
//            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "ai咨询已达上线");
//        }
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter pw = response.getWriter()) {
            // 使用策略模式处理请求
            aiModelContext.processRequest(model, pw, message,conversationId);
            pw.write("data:end\n\n");
            pw.flush();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 停止内容生成
     * @param conversationId 请求ID
     * @return 停止结果
     */
    @GetMapping("/stop-generation")
    @SaCheckLogin
    public ResponseEntity<Map<String, Object>> stopGeneration(String conversationId) {
        logger.info("接收到停止生成请求，请求ID: {}", conversationId);
        
        Map<String, Object> result = new HashMap<>();
        boolean success = aiModelContext.stopGeneration(conversationId);
        result.put("success", success);
        result.put("requestId", conversationId);
        result.put("message", success ? "成功停止生成" : "停止生成失败，可能请求已完成或不存在");
        return ResponseEntity.ok(result);
    }
}
