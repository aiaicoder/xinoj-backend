<template>
  <div class="chat-panel">
    <div class="message-panel" ref="messagePanelRef">
      <div class="message-list">
        <div
          :class="['message-item', item.type == 1 ? 'ai-item' : '']"
          v-for="(item, index) in messageList"
          :key="index"
          :id="'item' + index"
        >
          <template v-if="item.type == 0">
            <div class="message-content">
              <div class="content-inner">{{ item.content }}</div>
            </div>
            <div class="user-icon">
              <img :src="userAvatar" alt="用户头像" />
            </div>
          </template>
          <template v-else>
            <div class="user-icon">AI</div>
            <div class="message-content ai-item">
              <MdView
                :value="
                  Array.isArray(item.content)
                    ? item.content.join('')
                    : item.content.toString()
                "
                height="auto"
              />
              <div class="loading" v-if="item.loading">
                <div class="loading-dots">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
    <div class="send-panel">
      <div class="model-selector">
        <span class="model-label">模型选择</span>
        <a-select
          allow-clear
          placeholder="选择模型"
          v-model="formData.model"
          :style="{ width: '200px' }"
        >
          <a-option value="deepseek-r1:8b">deepseek-r1:8b</a-option>
          <a-option value="Pro/deepseek-ai/DeepSeek-V3">DeepSeekV3Por</a-option>
        </a-select>
      </div>

      <div class="input-container">
        <a-textarea
          :rows="1"
          :auto-size="{ minRows: 1, maxRows: 4 }"
          allow-clear
          placeholder="输入任何题，Enter发送，Shift + Enter 换行"
          v-model="formData.content"
          @keydown="keySend"
          class="chat-textarea"
        ></a-textarea>

        <div class="toolbar">
          <div class="button-container">
            <a-button
              v-if="!loading"
              type="primary"
              @click="sendMessage"
              :disabled="!formData.content.trim()"
              class="send-button"
              shape="circle"
              :class="{
                disabled: !formData.content.trim(),
                active: formData.content.trim(),
              }"
            >
              <template #icon>
                <svg
                  viewBox="0 0 24 24"
                  width="20"
                  height="20"
                  fill="currentColor"
                >
                  <path
                    d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8-8-8z"
                    transform="rotate(270 12 12)"
                  ></path>
                </svg>
              </template>
            </a-button>

            <a-button
              v-else
              type="primary"
              @click="stopCurrentGeneration"
              class="stop-button"
              shape="circle"
            >
              <template #icon>
                <div class="stop-icon-container">
                  <div class="stop-icon-square"></div>
                </div>
              </template>
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  defineExpose,
  defineProps,
  nextTick,
  onMounted,
  ref,
  watch,
} from "vue";
import MdView from "@/components/MdView.vue";
import { AiControllerService, QuestionVO } from "../../generated";
import message from "@arco-design/web-vue/es/message";
import { OpenAPI } from "../../generated";

interface Props {
  question?: QuestionVO;
  userAvatar?: string;
  active?: boolean;
}

const props = defineProps<Props>();

// 自动发送题目内容给AI的标记
const hasAutoAsked = ref(false);

// 默认用户头像
const defaultAvatarUrl = "https://avatars.githubusercontent.com/u/1?v=4";

// 用户头像来自props或默认值
const userAvatar = ref(defaultAvatarUrl);

// 监听props中的userAvatar变化
watch(
  () => props.userAvatar,
  (newVal) => {
    if (newVal) {
      userAvatar.value = newVal;
    } else {
      userAvatar.value = defaultAvatarUrl;
    }
    console.log("userAvatar changed:", newVal);
  },
  { immediate: true }
);

const messagePanelRef = ref<HTMLElement | null>(null);

const formData = ref({
  model: "deepseek-r1:8b",
  content: "",
});

interface MessageItem {
  type: number; // 0: user, 1: ai
  content: string | string[];
  loading?: boolean;
  conversationId?: string;
}

const messageList = ref<MessageItem[]>([
  {
    type: 1,
    content: ["你好！我是智能助手，可以回答你关于这道题目的任何问题。"],
    loading: false,
  },
]);

const loading = ref(false);
const currentConversationId = ref("");

// 自动向AI提问当前题目内容
const autoAskQuestion = () => {
  if (hasAutoAsked.value || !props.question || !props.active) return;

  // 构建提问内容：标题+题目内容
  const questionContent = props.question.content || "";
  const questionTitle = props.question.title || "";

  if (!questionContent || hasAutoAsked.value) return;

  const prompt = `请分析这道题目并给出解题思路和关键点：\n\n【题目】${questionTitle}\n\n【内容】${questionContent}`;

  // 设置到输入框
  formData.value.content = prompt;

  // 延迟一点发送，确保组件已完全挂载
  setTimeout(() => {
    sendMessage();
    hasAutoAsked.value = true;
  }, 500);
};

// 监听active属性变化
watch(
  () => props.active,
  (newVal) => {
    if (newVal && !hasAutoAsked.value) {
      autoAskQuestion();
    }
  },
  { immediate: true }
);

// 发送消息函数
const sendMessage = () => {
  const content = formData.value.content;
  if (!content || content.trim() === "") {
    message.warning("请输入内容");
    return;
  }
  
  messageList.value.push({
    type: 0,
    content: content,
  });

  // 生成唯一请求ID
  currentConversationId.value = generateConversationId();

  messageList.value.push({
    type: 1,
    content: [],
    loading: true,
    conversationId: currentConversationId.value,
  });
  
  loading.value = true;
  formData.value.content = "";

  nextTick(() => {
    scrollToBottom();
  });

  // 获取api基础路径
  const baseUrl = OpenAPI.BASE;
  
  // 构建SSE请求URL参数
  const params = new URLSearchParams({
    conversationId: currentConversationId.value,
    message: content,
    model: formData.value.model
  });
  
  // 构建完整的URL
  const url = `${baseUrl}/api/stream?${params.toString()}`;
  
  // 创建EventSource连接
  const eventSource = new EventSource(url);

  eventSource.onmessage = (event) => {
    let response = event.data;
    console.log(response);
    if (response === "end" || response === "[DONE]") {
      closeEventSource(eventSource);
      return;
    }
    
    try {
      response = JSON.parse(response).content;
      const lastIndex = messageList.value.length - 1;
      if (Array.isArray(messageList.value[lastIndex].content)) {
        (messageList.value[lastIndex].content as string[]).push(response);
      }
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom();
      });
    } catch (error) {
      console.error("解析响应出错:", error);
    }
  };

  eventSource.onerror = (error) => {
    console.error("SSE连接错误:", error);
    closeEventSource(eventSource);
    message.error("连接服务器失败，请检查网络或服务器状态");
  };
};

// 暴露给父组件的方法
defineExpose({
  autoAskQuestion,
});

// 当收到问题数据时，添加一条系统消息
onMounted(() => {
  if (props.question && props.question.title) {
    messageList.value.push({
      type: 1,
      content: [
        `当前题目：**${props.question.title}**\n\n我可以帮你解答关于这道题的疑问，包括思路讲解、算法分析等。请问有什么需要帮助的吗？`,
      ],
      loading: false,
    });
  }
  scrollToBottom();
});

const keySend = (event: KeyboardEvent) => {
  if (!(event.ctrlKey && event.key === "Enter")) {
    return;
  }
  sendMessage();
};

const closeEventSource = (eventSource: EventSource) => {
  eventSource.close();
  const lastIndex = messageList.value.length - 1;
  if (lastIndex >= 0) {
    messageList.value[lastIndex].loading = false;
  }
  loading.value = false;
  currentConversationId.value = "";
};

// 生成唯一请求ID
const generateConversationId = (): string => {
  return Array.from(window.crypto.getRandomValues(new Uint8Array(16)))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
};

// 停止当前正在生成的内容
const stopCurrentGeneration = () => {
  if (currentConversationId.value) {
    // 使用AiControllerService提供的方法停止生成
    AiControllerService.stopGenerationUsingGet(currentConversationId.value)
      .then(response => {
        if (response.success) {
          message.success("已停止生成");
          
          // 更新消息列表中对应消息的loading状态
          const messageIndex = messageList.value.findIndex(
            (item) => item.conversationId === currentConversationId.value
          );
          if (messageIndex !== -1) {
            messageList.value[messageIndex].loading = false;
          }
          loading.value = false;
        } else {
          message.warning(response.message || "停止生成失败");
        }
      })
      .catch(error => {
        console.error("停止生成失败:", error);
        message.error("停止生成请求失败");
      });
  }
};

// 滚动到底部
const scrollToBottom = () => {
  if (messagePanelRef.value) {
    messagePanelRef.value.scrollTop = messagePanelRef.value.scrollHeight;
  }
};

</script>

<style scoped>
.chat-panel {
  background: #f5f5f5;
  height: 600px;
  display: flex;
  flex-direction: column;
  border-radius: 4px;
  overflow: hidden;

  .message-panel {
    position: relative;
    flex: 1;
    overflow: auto;
    padding-bottom: 10px;

    .message-list {
      margin: 0 auto;
      width: 100%;
      max-width: 800px;
      padding: 0 16px;

      .message-item {
        margin: 16px 0;
        display: flex;

        .user-icon {
          width: 36px;
          height: 36px;
          line-height: 36px;
          border-radius: 50%;
          background: #535353;
          color: #fff;
          text-align: center;
          margin-left: 10px;
          flex-shrink: 0;
          overflow: hidden;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .user-icon img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .message-content {
          flex: 1;
          margin-left: 10px;
          align-items: center;
          display: flex;
          justify-content: flex-end;
        }

        .content-inner {
          background: #165dff;
          border-radius: 8px;
          padding: 12px;
          color: #fff;
          max-width: 80%;
        }
      }

      .ai-item {
        line-height: 23px;

        .message-content {
          display: block;
          background: #fff;
          border-radius: 8px;
          padding: 0;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        }

        .user-icon {
          background: #165dff;
          margin-left: 0;
        }

        :deep(.md-view-container) {
          border: none;
          padding: 12px;
        }

        .loading {
          text-align: center;
          padding: 8px 0;

          .loading-dots {
            display: flex;
            justify-content: center;
            margin-bottom: 8px;

            .dot {
              display: inline-block;
              width: 8px;
              height: 8px;
              border-radius: 50%;
              background-color: #165dff;
              margin: 0 3px;
              animation: dot-flashing 1s infinite alternate;
            }

            .dot:nth-child(2) {
              animation-delay: 0.2s;
            }

            .dot:nth-child(3) {
              animation-delay: 0.4s;
            }
          }
        }
      }
    }
  }

  .send-panel {
    padding: 16px;
    background: #fff;
    border-top: 1px solid #e5e6eb;

    .model-selector {
      display: flex;
      align-items: center;
      margin-bottom: 12px;

      .model-label {
        color: #f64b4b;
        font-weight: 500;
        margin-right: 10px;
      }
    }

    .input-container {
      width: 100%;
      background: #fff;
      border-radius: 16px;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
      padding: 12px 16px 0;

      .chat-textarea {
        width: 100%;

        :deep(.arco-textarea-wrapper) {
          border: none;
          background: transparent;

          &:focus-within {
            box-shadow: none;
          }
        }

        :deep(.arco-textarea) {
          padding: 0;
          font-size: 14px;
          resize: none;
        }
      }

      .toolbar {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        padding: 6px 0 8px;
      }

      .button-container {
        display: flex;
        align-items: center;
      }
    }
  }

  .toolbar {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    padding: 6px 0 8px;
  }
  
  .button-container {
    display: flex;
    align-items: center;
  }
  
  .send-button,
  .stop-button {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
  }
  
  .send-button.active {
    background-color: #000;
    border-color: #000;
    
    &:hover {
      transform: scale(1.05);
      background-color: #333;
      border-color: #333;
    }
  }
  
  .send-button.disabled {
    background-color: #d9d9d9;
    border-color: #d9d9d9;
    cursor: not-allowed;
    
    &:hover {
      transform: none;
      background-color: #d9d9d9;
      border-color: #d9d9d9;
    }
  }
  
  .stop-button {
    background-color: #f56c6c;
    border-color: #f56c6c;
    
    &:hover {
      transform: scale(1.05);
      background-color: #f78989;
      border-color: #f78989;
    }
  }
}

@keyframes dot-flashing {
  0% {
    opacity: 0.2;
  }
  100% {
    opacity: 1;
  }
}

.stop-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

.stop-icon-square {
  width: 12px;
  height: 12px;
  background-color: white;
  border-radius: 2px;
}
</style>
