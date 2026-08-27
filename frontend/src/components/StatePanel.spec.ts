import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import StatePanel from './StatePanel.vue'

describe('StatePanel', () => {
  it('展示状态并触发恢复动作', async () => {
    const wrapper = mount(StatePanel, { props: { title: '加载失败', description: '请稍后重试', action: '重试' } })
    expect(wrapper.text()).toContain('加载失败')
    await wrapper.get('button').trigger('click')
    expect(wrapper.emitted('action')).toHaveLength(1)
  })
})
