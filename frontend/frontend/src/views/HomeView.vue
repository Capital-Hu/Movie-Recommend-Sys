<template>
  <div>
    <login v-if="currentComponent === 'login'" @change-component="handleChangeComponent" />
    <register v-if="currentComponent === 'register'" @change-component="handleChangeComponent" />
  </div>
</template>

<script>
import login from '@/pages/login/login';
import register from '@/pages/register/register';

export default {
  name: 'HomeView',
  components: {
    login,
    register
  },
  data() {
    return {
      currentComponent: 'login' // 默认显示 login 组件
    };
  },
  methods: {
    handleChangeComponent(componentName) {
      this.currentComponent = componentName;
    }
  }
}
</script>
