<template>
  <div class="md-view-container">
    <Viewer :value="value" :plugins="plugins"/>
  </div>
</template>

<script setup lang="ts">
import gfm from "@bytemd/plugin-gfm";
import { Viewer } from "@bytemd/vue-next";
import { defineProps, withDefaults } from "vue";
import highlight from "@bytemd/plugin-highlight";

const plugins = [
  gfm(),
  // Add more plugins here
  highlight(),
];

// 定义Props属性
interface Props {
  value: string;
  height?: string;
}

const props = withDefaults(defineProps<Props>(), {
  value: "",
  height: "600px"
});
</script>

<style scoped>
.md-view-container {
  max-height: v-bind('props.height');
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 8px;
  scrollbar-width: none; /* Firefox */
}

.md-view-container::-webkit-scrollbar {
  width: 6px;
  display: none;
}

.md-view-container:hover::-webkit-scrollbar {
  display: block;
}

.md-view-container::-webkit-scrollbar-thumb {
  background-color: #c0c0c0;
  border-radius: 3px;
}

.md-view-container::-webkit-scrollbar-track {
  background: transparent;
}
</style>
