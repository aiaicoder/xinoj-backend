package com.xin.xinoj.common;

/**
 * 自定义错误码
 *
 * @author <a href="https://github.com/liyupi">程序员小新</a>
 * 
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    API_REQUEST_ERROR(50010,"远程接口调用失败"),
    TOO_MANY_REQUEST(50011,"请求超过限制"),

    INVALID_TOKEN_ERROR(401002,"登录失效"),

    BE_REPLACED_MESSAGE(401003,"账号异地登录"),

    TOKEN_TIMEOUT_MESSAGE(401004,"登录过期"),
    KICK_OUT_ERROR(401005,"你已被踢下线"),
    TOKEN_FREEZE_ERROR(401006,"账号已被冻结");


    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
