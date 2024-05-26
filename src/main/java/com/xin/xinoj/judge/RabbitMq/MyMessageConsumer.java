package com.xin.xinoj.judge.RabbitMq;

import com.rabbitmq.client.Channel;
import com.xin.xinoj.common.ErrorCode;
import com.xin.xinoj.constant.MqConstant;
import com.xin.xinoj.exception.BusinessException;
import com.xin.xinoj.judge.JudgeService;
import com.xin.xinoj.model.entity.Question;
import com.xin.xinoj.model.entity.QuestionSubmit;
import com.xin.xinoj.model.enums.QuestionSubmitVisibleEnum;
import com.xin.xinoj.service.QuestionService;
import com.xin.xinoj.service.QuestionSubmitService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 15712
 */
@Component
@Slf4j
public class MyMessageConsumer {

    @Resource
    private JudgeService judgeService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionService questionService;

    /**
     * 接受消息
     *
     * @param message
     */
    @SneakyThrows
    @RabbitListener(queues = MqConstant.DIRECT_QUEUE1, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        if (message == null) {
            // 消息为空，则拒绝消息（不重试），进入死信队列
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "消息为空");
        }
        long questionSubmitId = Long.parseLong(message);
        try {
            judgeService.doJudge(questionSubmitId);
            QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
            if (!questionSubmit.getStatus().equals(QuestionSubmitVisibleEnum.SUCCESS.getValue())) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "判题失败");
            }
            log.info("新提交的信息：" + questionSubmit);
            // 设置通过数
            Long questionId = questionSubmit.getQuestionId();
            log.info("题目:" + questionId);
            Question question = questionService.getById(questionId);
            Integer acceptedNum = question.getAcceptedNum();
            Question updateQuestion = new Question();
            synchronized (question.getAcceptedNum()) {
                acceptedNum = acceptedNum + 1;
                updateQuestion.setId(questionId);
                updateQuestion.setAcceptedNum(acceptedNum);
                boolean save = questionService.updateById(updateQuestion);
                if (!save) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存数据失败");
                }
            }
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "判题失败");
        }

    }
}
