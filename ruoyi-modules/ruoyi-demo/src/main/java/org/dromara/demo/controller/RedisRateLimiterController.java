package org.dromara.demo.controller;

import org.dromara.common.core.domain.RequestResponse;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 测试分布式限流样例
 *
 * @author Lion Li
 */
@Slf4j
@RestController
@RequestMapping("/demo/rateLimiter")
public class RedisRateLimiterController {

    /**
     * 测试全局限流
     * 全局影响
     */
    @RateLimiter(count = 2, time = 10)
    @GetMapping("/test")
    public RequestResponse<String> test(String value) {
        return RequestResponse.ok("操作成功", value);
    }

    /**
     * 测试请求IP限流
     * 同一IP请求受影响
     */
    @RateLimiter(count = 2, time = 10, limitType = LimitType.IP)
    @GetMapping("/testip")
    public RequestResponse<String> testip(String value) {
        return RequestResponse.ok("操作成功", value);
    }

    /**
     * 测试集群实例限流
     * 启动两个后端服务互不影响
     */
    @RateLimiter(count = 2, time = 10, limitType = LimitType.CLUSTER)
    @GetMapping("/testcluster")
    public RequestResponse<String> testcluster(String value) {
        return RequestResponse.ok("操作成功", value);
    }

    /**
     * 测试请求IP限流(key基于参数获取)
     * 同一IP请求受影响
     *
     * 简单变量获取 #变量 复杂表达式 #{#变量 != 1 ? 1 : 0}
     */
    @RateLimiter(count = 2, time = 10, limitType = LimitType.IP, key = "#value")
    @GetMapping("/testObj")
    public RequestResponse<String> testObj(String value) {
        return RequestResponse.ok("操作成功", value);
    }

}
