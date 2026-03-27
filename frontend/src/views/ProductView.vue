<template>
  <div class="product-page">
    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="routes" tab="线路列表">
        <route-view />
      </a-tab-pane>

      <a-tab-pane key="departures" tab="团期列表">
        <departure-view />
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import RouteView from './product/RouteView.vue';
import DepartureView from './product/DepartureView.vue';

const activeTab = ref('routes');
</script>

<style scoped>
.product-page {
  padding: 0;
}
</style>
