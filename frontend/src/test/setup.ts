import { config } from '@vue/test-utils';

// Configure global stubs for Ant Design Vue components that are complex to render
config.global.stubs = {
  'a-card': {
    template: '<div class="a-card"><slot name="title" /><slot /></div>'
  },
  'a-checkbox': {
    template: '<input type="checkbox" :checked="checked" :indeterminate="indeterminate" @change="$emit(\'change\', $event)" v-bind="$attrs" />',
    props: ['checked', 'indeterminate']
  },
  'a-input': {
    template: '<input :value="value" @input="$emit(\'update:value\', $event.target.value)" v-bind="$attrs" />',
    props: ['value']
  },
  'a-button': {
    template: '<button @click="$emit(\'click\')" :disabled="disabled" v-bind="$attrs"><slot /></button>',
    props: ['disabled', 'loading', 'type']
  },
  'a-form': {
    template: '<form><slot /></form>'
  },
  'a-form-item': {
    template: '<div class="a-form-item"><slot /></div>'
  },
  'a-empty': {
    template: '<div class="a-empty">{{ description }}</div>',
    props: ['description', 'image']
  },
  'a-tag': {
    template: '<span class="a-tag"><slot /></span>',
    props: ['color']
  },
  'a-tabs': {
    template: '<div class="a-tabs"><slot /></div>'
  },
  'a-tab-pane': {
    template: '<div class="a-tab-pane"><slot /></div>',
    props: ['key', 'tab']
  },
  'a-table': {
    template: '<table class="a-table"></table>',
    props: ['columns', 'dataSource', 'loading', 'pagination']
  },
  'a-drawer': {
    template: '<div class="a-drawer" v-if="open"><slot /><slot name="extra" /></div>',
    props: ['open', 'title', 'placement', 'width']
  },
  'a-space': {
    template: '<div class="a-space"><slot /></div>'
  },
  'a-tooltip': {
    template: '<div class="a-tooltip"><slot /></div>',
    props: ['title']
  },
  'a-popconfirm': {
    template: '<div class="a-popconfirm"><slot /></div>',
    props: ['title']
  },
  'page-container': {
    template: '<div class="page-container"><slot /></div>'
  },
  'permission-tree-panel': {
    template: '<div class="permission-tree-panel" />'
  },
  'search-outlined': { template: '<span />' },
  'plus-outlined': { template: '<span />' },
  'reload-outlined': { template: '<span />' },
  'edit-outlined': { template: '<span />' },
  'delete-outlined': { template: '<span />' }
};
