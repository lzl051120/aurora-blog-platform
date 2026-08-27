<script setup lang="ts">
defineProps<{ title: string; description: string; action?: string }>()
defineEmits<{ action: [] }>()
</script>
<template>
  <section class="state-panel" role="status">
    <span class="state-symbol" aria-hidden="true">✦</span><h2>{{ title }}</h2><p>{{ description }}</p>
    <button v-if="action" class="button secondary" type="button" @click="$emit('action')">{{ action }}</button>
  </section>
</template>
