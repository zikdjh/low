<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="login-content">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="brand-content">
          <h1 class="brand-title">
            <t-icon name="logo-vue" size="48px" class="brand-icon"/>
            欢迎来到系统
          </h1>
          <p class="brand-description">
            为您提供优质的服务体验
          </p>
          <div class="feature-list">
            <div class="feature-item">
              <t-icon name="check-circle"/>
              <span>安全可靠</span>
            </div>
            <div class="feature-item">
              <t-icon name="check-circle"/>
              <span>简单易用</span>
            </div>
            <div class="feature-item">
              <t-icon name="check-circle"/>
              <span>高效便捷</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="form-section">
        <div class="form-container">
          <Login @success="handleLoginSuccess"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted} from 'vue';
import {useRouter} from 'vue-router';
import {MessagePlugin} from 'tdesign-vue-next';
import {useSettingStore} from '../../store';
import Login from './components/Login.vue';
import type {ResponseType} from '../../api/model/ResponseType.ts';

const router = useRouter();
const settingStore = useSettingStore();

// 确保登录页面也能正确应用主题
onMounted(() => {
  // 如果需要，可以在这里强制更新主题
  settingStore.changeBrandTheme(settingStore.brandTheme);
});

// 处理登录成功
const handleLoginSuccess = (response: ResponseType) => {
  MessagePlugin.success('登录成功！');
  // 存储用户信息到 localStorage 或 Pinia
  if (response.data?.token) {
    localStorage.setItem('token', response.data.token);
  }
  if (response.data?.userInfo) {
    localStorage.setItem('userInfo', JSON.stringify(response.data.userInfo));
  }

  // 跳转到首页
  router.push('/');
};
</script>

<style scoped lang="less">
@import "index.less";
</style>