<template>
  <div id="viewQuestionView">
    <a-row :gutter="[24, 24]">
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="question" @change="handleTabChange">
          <a-tab-pane key="question" title="题目">
            <a-card v-if="question" :title="question.title">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question?.judgeConfig?.timeLimit ?? 0 }} ms
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question?.judgeConfig?.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question?.judgeConfig?.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdView :value="question.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    :color="
                      handleColor(question.tags ? question.tags[index] : '')
                    "
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <a-tab-pane key="comment" title="评论区">
            <Comment :questionId="questionId" />
          </a-tab-pane>
          <a-tab-pane key="answer" title="答案">
            <MdView :value="question?.answer || ''" />
          </a-tab-pane>
          <a-tab-pane key="ai" title="智能AI问答">
            <AiChat 
              :question="question" 
              :user-avatar="userInfo.userAvatar"
              :active="activeTab === 'ai'"
              ref="aiChatRef"
            />
          </a-tab-pane>
          <a-tab-pane key="mySubmit" title="提交记录">
            <SubmissionRecord :shouldReload="shouldReload" />
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24" style="margin-top: 40px">
        <a-form :model="form" layout="inline">
          <a-form-item
            field="language"
            label="编程语言"
            style="min-width: 240px"
          >
            <a-select
              :style="{ width: '320px' }"
              placeholder="请选择语言"
              v-model="form.language"
            >
              <a-option v-for="(language, index) in languages" :key="index"
                >{{ language }}
              </a-option>
            </a-select>
          </a-form-item>
        </a-form>
        <a-divider size="0" />
        <CodeEditor
          :code="form.code"
          :language="form.language"
          :handle-change="codeHandleChange"
        />
        <a-divider size="0" />
        <a-button status="success" style="min-width: 200px" @click="doSubmit"
          >提交代码
        </a-button>
      </a-col>
    </a-row>
  </div>
  <a-modal
    v-model:visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    draggable
    :esc-to-close="false"
    :mask-closable="false"
  >
    <template #title> {{ modalTitle }}</template>
    <div v-if="judgeTime">耗时: {{ judgeTime }} ms</div>
    <div v-if="judgeMemory">内存消耗: {{ judgeMemory }} kb</div>
  </a-modal>
</template>

<script setup lang="ts">
import {
  onMounted,
  defineProps,
  withDefaults,
  ref,
  watchEffect,
  watch,
} from "vue";
import { QuestionControllerService, QuestionVO } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdView from "@/components/MdView.vue";
import Comment from "@/components/Comment.vue";
import { useRoute } from "vue-router";
import SubmissionRecord from "@/components/SubmissionRecord.vue";
import AiChat from "@/components/AiChat.vue";
import { useStore } from "vuex";
interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});
const visible = ref(false);
const modalTitle = ref("判题中...");
const judgeTime = ref<number>();
const judgeMemory = ref<number>();
let timer: ReturnType<typeof setInterval>;
let elapsedTime = 0;
const question = ref<QuestionVO>();
const shouldReload = ref(false);
const loadData = async () => {
  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    props.id as any
  );
  if (res.code === 0) {
    question.value = res.data;
  } else {
    message.error("加载失败," + res.message);
  }
};

const form = ref({
  language: "java",
  code: question.value?.codeTemplate?.java,
});

watch(
  () => form.value.language,
  (language) => {
    if (language === "java") {
      form.value.code = question.value?.codeTemplate?.java;
    } else if (language === "cpp") {
      form.value.code = question.value?.codeTemplate?.cpp;
    } else if (language === "python") {
      form.value.code = question.value?.codeTemplate?.python;
    } else if (language === "c") {
      form.value.code = question.value?.codeTemplate?.c;
    }
  }
);

// 从Vuex获取用户信息
const store = useStore();

// 获取用户信息
const userInfo = ref(store.getters['user/getUserInfo'] || {
  userAvatar: '',
  userName: '未登录'
});

onMounted(() => {
  getLanguages();
  loadData();
  
  // 如果用户信息还没加载，则加载用户信息
  if (!userInfo.value.id) {
    store.dispatch('user/getLoginUser').then(() => {
      userInfo.value = store.getters['user/getUserInfo'];
      console.log('获取用户信息成功:', userInfo.value);
    });
  }
});

/**
 * 同步获取判题信息
 */
const handleOk = () => {
  visible.value = false;
  if (timer) {
    clearInterval(timer);
    modalTitle.value = "判题中...";
    judgeTime.value = undefined;
  }
};
const handleCancel = () => {
  visible.value = false;
  if (timer) {
    clearInterval(timer);
  }
  modalTitle.value = "判题中...";
  judgeTime.value = undefined;
};

const startTimer = (id: number) => {
  timer = setInterval(async () => {
    elapsedTime++;
    // 10分钟，60秒 * 10
    if (elapsedTime >= 600) {
      clearInterval(timer);
    }
    const res = await QuestionControllerService.getJudgeResultUsingGet(id);
    if (res.code === 0) {
      clearInterval(timer);
      shouldReload.value = !shouldReload.value;
      if (res?.data?.status === 2) {
        modalTitle.value = "判题成功";
        judgeTime.value = res?.data?.judgeInfo?.time;
        judgeMemory.value = res?.data?.judgeInfo?.memory;
        return;
      } else {
        modalTitle.value = "解答错误or编译错误";
        judgeTime.value = res?.data?.judgeInfo?.time;
        judgeMemory.value = res?.data?.judgeInfo?.memory;
      }
    }
  }, 3000);
};

/**
 * 获取编程语言
 */
const languages = ref<string[]>([]);

const getLanguages = async () => {
  const res = await QuestionControllerService.getAllLanguagesUsingGet();
  if (res.code === 0) {
    languages.value = res.data as string[];
  } else {
    message.error("加载失败," + res.message);
  }
};

const route = useRoute();
const questionId = route.params.id;

watchEffect(() => {
  loadData();
});

const handleColor = (record: string): string => {
  if (record === "困难") {
    return "red";
  } else if (record === "中等") {
    return "orange";
  } else if (record === "简单") {
    return "green";
  } else {
    return "gray";
  }
};

const codeHandleChange = (val: string) => {
  form.value = {
    ...form.value,
    code: val,
  };
};

/**
 * 执行搜索
 */
const doSubmit = async () => {
  const res = await QuestionControllerService.doQuestionSubmitUsingPost({
    ...form.value,
    questionId: question.value?.id,
  });
  if (res.code === 0) {
    message.success("提交成功");
  } else {
    message.error("提交失败," + res.message);
  }
  visible.value = true;
  startTimer(res.data);
};

// 当前激活的选项卡
const activeTab = ref('question');
const aiChatRef = ref(null);

// 处理选项卡变化
const handleTabChange = (key: string) => {
  activeTab.value = key;
  
  // 如果切换到AI问答选项卡，延迟一点执行自动提问（确保组件已挂载）
  if (key === 'ai' && question.value) {
    setTimeout(() => {
      if (aiChatRef.value) {
        // 使用类型断言解决类型问题
        const aiChat = aiChatRef.value as any;
        if (typeof aiChat.autoAskQuestion === 'function') {
          aiChat.autoAskQuestion();
        }
      }
    }, 100);
  }
};
</script>

<style>
#viewQuestionView .arco-card-size-medium .arco-card-header-extra {
  margin-top: 10px;
}
</style>
