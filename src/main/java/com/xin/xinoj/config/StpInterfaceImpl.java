package com.xin.xinoj.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;

import com.xin.xinoj.constant.UserConstant;
import com.xin.xinoj.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限认证接口扩展，Sa-Token 将从此实现类获取每个账号拥有的权限码 
 * 
 * @author click33
 * @since 2022-10-13
 */
@Component	// 打开此注解，保证此类被springboot扫描，即可完成sa-token的自定义权限验证扩展 
public class StpInterfaceImpl implements StpInterface {

	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		return null;
	}

	/**
	 * 返回一个账号所拥有的角色标识集合 
	 */
	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		// 本list仅做模拟，实际项目中要根据具体业务逻辑来查询角色
		List<String> list = new ArrayList<String>();
		//获取当前用户的登录信息，判断是否有管理员权限
		User user = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);
		if(user.getUserRole().equals(UserConstant.ADMIN_ROLE)){
			list.add(UserConstant.ADMIN_ROLE);
		}else {
			list.add(UserConstant.DEFAULT_ROLE);
		}
		return list;
	}

}
