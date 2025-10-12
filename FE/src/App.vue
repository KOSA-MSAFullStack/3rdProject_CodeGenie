<template>
  <div class="wrap">
    <Sidebar />
    <main class="content">
      <AppHeader />
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import AppHeader from './components/AppHeader.vue'
</script>

<style>
* { box-sizing: border-box }
html, body, #app { height: 100%; margin: 0 }
body {
  font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, 'Noto Sans KR', Arial;
  background: #f7f7f9;
}
.wrap { display: flex; min-height: 100vh }
.content { flex: 1; display: flex; flex-direction: column }
</style>
