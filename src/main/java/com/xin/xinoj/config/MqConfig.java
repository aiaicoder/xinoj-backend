package com.xin.xinoj.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.xin.xinoj.constant.MqConstant.*;

/**
 * @author 15712
 */
@Configuration
public class MqConfig {

    /**
     * 声明交换机
     *
     * @return Direct类型交换机
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    /**
     * 队列
     */
    @Bean
    public Queue directQueue1() {
        Queue queue = new Queue(DIRECT_QUEUE1);
        queue.addArgument("x-dead-letter-exchange", DLX_EXCHANGE);
        queue.addArgument("x-dead-letter-routing-key", "ojCode");
        return queue;
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue dlxQueue() {
        return new Queue(DLX_QUEUE);
    }

    /**
     * 绑定队列
     */
    @Bean
    public Binding bindingQueue1WithRed(Queue directQueue1, DirectExchange directExchange) {
        return BindingBuilder.bind(directQueue1).to(directExchange).with("oj");
    }

    /**
     * 绑定死信队列和死信交换机
     *
     * @param dlxQueue
     * @param dlxExchange
     * @return
     */
    @Bean
    public Binding dlxQueueBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(dlxQueue).to(dlxExchange).with("ojCode");
    }
}
