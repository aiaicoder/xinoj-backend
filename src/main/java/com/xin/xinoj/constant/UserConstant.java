package com.xin.xinoj.constant;

/**
 * 用户常量
 *
 * @author <a href="https://github.com/liyupi">程序员小新</a>
 * 
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    String DEFAULT_AVATAR ="https://my-notes-li.oss-cn-beijing.aliyuncs.com/li/4d7582688ef167dd8c910c111f22dae.jpg";

    /**
     * 默认随机生成用户名
     */
    String DEFAULT_USERNAME = "算法家-"+String.valueOf(System.currentTimeMillis()).substring(0,5);
}
