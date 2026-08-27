import AxeBuilder from '@axe-core/playwright'
import { expect, test } from '@playwright/test'

for (const path of ['/', '/posts', '/auth']) {
  test(`${path} 满足 WCAG 2.2 AA 自动扫描`, async ({ page }) => {
    await page.goto(path)
    await expect(page.locator('body')).toBeVisible()
    await expect(page.locator('.skeleton-grid')).toHaveCount(0)
    const results = await new AxeBuilder({ page })
      .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
      .analyze()
    expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([])
  })
}

test('首页支持键盘跳转和 200% 缩放', async ({ page }) => {
  await page.goto('/')
  await page.keyboard.press('Tab')
  await expect(page.getByRole('link', { name: '跳到主要内容' })).toBeFocused()
  await page.getByRole('link', { name: '跳到主要内容' }).press('Enter')
  await expect(page.locator('#main-content')).toBeFocused()
  await page.setViewportSize({ width: 720, height: 500 })
  await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth + 1)).toBe(true)
})
