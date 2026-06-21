<template>
  <div class="time-element">
    <t-time-picker v-model="value" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const value = ref('');
</script>