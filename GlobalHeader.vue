<template>
  <a-row class="grid-demo" align="center" :wrap="false">
    <a-col flex="auto">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '38px' }"
          disabled
        >
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="图标" />
            <div class="title">新 OJ</div>
          </div>
        </a-menu-item>
        <a-menu-item v-for="item in showRoutes" :key="item.path">
          {{ item.name }}
        </a-menu-item>
      </a-menu>
    </a-col>
    <a-col flex="100px">
      <LoginAndRegister
        v-if="store.state.user.loginUser.userRole === Access_Enum.UnLogin"
      ></LoginAndRegister>
      <a-dropdown position="bottom">
        <a-avatar
          v-if="store.state.user.loginUser.userRole !== Access_Enum.UnLogin"
        >
          <img
            alt="avatar"
            :src="
              loginUser.userAvatar ??
              'https://xinbo-1314980040.cos.ap-beijing.myqcloud.com/test%2Fimg2.jpg'
            "
          />
        </a-avatar>
        <template #content>
          <a-doption @click="handleClick">个人简介</a-doption>
          <a-doption @click="logout">登出</a-doption>
        </template>
      </a-dropdown>
    </a-col>
  </a-row>
  <!-- 对话框 -->
  <a-modal v-model:visible="visible" @ok="handleOk" :footer="false">
    <template #title>用户简介</template>
    <div>
      <a-descriptions
        size="mini"
        layout="inline-vertical"
        :data="data"
        bordered
      />
    </div>
    <a-button @click="handleCancel" style="margin-top: 10px">
      <router-link to="/user/center">修改个人信息</router-link>
    </a-button>
  </a-modal>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { routes } from "@/router/routes";
import { computed, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import Access_Enum from "@/access/ACCESS_ENUM";
import LoginAndRegister from "@/components/LoginAndRegister.vue";
import message from "@arco-design/web-vue/es/message";
import moment from "moment/moment";

//默认主页
const selectedKeys = ref(["/"]);
//router信息对象，route是当前路由参数
const router = useRouter();
const store = useStore();
const loginUser = computed(() => {
  return store.state.user.loginUser;
});

router.afterEach((to) => {
  selectedKeys.value = [to.path];
});

  const showRoutes = computed(() => {
    return routes.filter((route) => {
      if (route.meta?.hideInMenu) {
        return false;
      }
      return checkAccess(
        //如果直接通过复制拿到的值，不是响应式数据
        store.state.user.loginUser,
        route.meta?.access as string
      );
    });
  });

const doMenuClick = (key: string) => {
  router.push(key);
};

const logout = async () => {
  await store.dispatch("user/userLoginOut", {});
  message.success("登出成功");
  location.reload();
};

const visible = ref(false);
const handleClick = () => {
  visible.value = true;
};
const handleOk = () => {
  visible.value = false;
};
const handleCancel = () => {
  visible.value = false;
};

const data = computed(() => {
  return [
    {
      label: "用户名称",
      value: loginUser.value.userName,
    },
    {
      label: "id",
      value: loginUser.value.id,
    },
    {
      label: "性别",
      value: loginUser.value.gender == 1 ? "男" : "女",
    },
    {
      label: "用户简介",
      value: loginUser.value.userProfile,
    },
    {
      label: "注册时间",
      value: moment(loginUser.value.createTime).format("YYYY-MM-DD hh:mm"),
    },
  ];
});
</script>

<style scoped>
.title-bar .logo {
  height: 30px;
}

.router-link-active {
  text-decoration: none;
}

a {
  text-decoration: none;
}

.title-bar {
  display: flex;
}

.title-bar .title {
  margin-left: 20px;
  font-size: 20px;
  color: black;
}
</style>