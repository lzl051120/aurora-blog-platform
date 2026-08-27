<script setup lang="ts">import StatePanel from '../components/StatePanel.vue'</script>
<template><div class="public-page not-found"><StatePanel title="404 · 这页走丢了" description="它也许被移走了，或者从未存在。" action="回到首页" @action="$router.push('/')" /></div></template>
