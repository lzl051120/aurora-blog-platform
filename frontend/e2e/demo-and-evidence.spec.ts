import { expect, test } from '@playwright/test'

const adminUser = process.env.E2E_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.E2E_ADMIN_PASSWORD || 'ChangeMe123!'

test('采集公开端和后台真实渲染证据', async ({ page }, testInfo) => {
  await page.goto('/')
  await expect(page.locator('.skeleton-grid')).toHaveCount(0)
  await page.screenshot({ path: `../docs/evidence/home-${testInfo.project.name}.png`, fullPage: true })

  if (testInfo.project.name !== 'desktop-chromium') return
  await page.goto('/auth')
  await page.getByLabel('用户名或邮箱').fill(adminUser)
  await page.getByLabel('密码').fill(adminPassword)
  await page.getByRole('button', { name: '进入 Aurora' }).click()
  await expect(page).toHaveURL(/\/admin/)
  await page.screenshot({ path: `../docs/evidence/admin-${testInfo.project.name}.png`, fullPage: true })
})

test('管理员上传真实图片并验证媒体库', async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== 'desktop-chromium', '媒体上传只保留一份桌面证据')
  await page.goto('/auth')
  await page.getByLabel('用户名或邮箱').fill(adminUser)
  await page.getByLabel('密码').fill(adminPassword)
  await page.getByRole('button', { name: '进入 Aurora' }).click()
  await page.getByRole('button', { name: '媒体' }).click()
  await page.locator('input[type=file]').setInputFiles('src/assets/hero.png')
  await expect(page.getByText('图片已上传')).toBeVisible()
  await expect(page.getByText('hero.png').first()).toBeVisible()
})

test('生成管理员发布到游客阅读的连贯演示', async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== 'desktop-chromium', '演示录像只生成桌面版本')
  const suffix = Date.now().toString().slice(-7)
  const title = `Aurora 演示文章 ${suffix}`
  await page.goto('/')
  await page.waitForTimeout(700)
  await page.goto('/auth')
  await page.getByLabel('用户名或邮箱').fill(adminUser)
  await page.getByLabel('密码').fill(adminPassword)
  await page.getByRole('button', { name: '进入 Aurora' }).click()
  await page.getByRole('link', { name: /新建文章/ }).click()
  await page.getByPlaceholder('文章标题').fill(title)
  await page.getByPlaceholder(/值得读完/).fill('这是一篇用于课程答辩演示的真实全栈文章。')
  await page.locator('.tiptap').fill('内容经过 Spring Boot 安全清洗并写入 MySQL，随后由游客从公开站搜索和阅读。')
  await page.getByRole('button', { name: '发布文章' }).click()
  await expect(page).toHaveURL(/\/admin$/)
  await page.context().clearCookies()
  await page.goto('/posts')
  await page.getByPlaceholder('搜索标题或摘要').fill(suffix)
  await page.getByRole('button', { name: '筛选' }).click()
  await page.getByText(title).click()
  await expect(page.getByText('内容经过 Spring Boot 安全清洗并写入 MySQL')).toBeVisible()
  await page.waitForTimeout(1200)
})
