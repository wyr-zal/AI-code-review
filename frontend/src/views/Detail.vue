<template>
  <div class="detail-page">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">审查详情</span>
      </template>
    </el-page-header>

    <div v-loading="loading" class="detail-content">
      <el-card shadow="hover" class="info-card">
        <template #header>
          <span>基本信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID">
            {{ taskDetail.id }}
          </el-descriptions-item>
          <el-descriptions-item label="标题">
            {{ taskDetail.title }}
          </el-descriptions-item>
          <el-descriptions-item label="编程语言">
            {{ taskDetail.language }}
          </el-descriptions-item>
          <el-descriptions-item label="AI模型">
            {{ taskDetail.aiModel }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(taskDetail.status)">
              {{ getStatusText(taskDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(taskDetail.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="hover" class="code-card">
        <template #header>
          <span>代码内容</span>
        </template>
        <pre class="code-content">{{ taskDetail.codeContent }}</pre>
      </el-card>

      <el-card v-if="taskDetail.status === 2" shadow="hover" class="result-card">
        <template #header>
          <span>审查结果</span>
        </template>

        <div class="score-section">
          <h3>评分</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="score-item">
                <div class="score-label">质量评分</div>
                <el-progress
                  type="circle"
                  :percentage="taskDetail.qualityScore"
                  :color="getProgressColor(taskDetail.qualityScore)"
                />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="score-item">
                <div class="score-label">安全评分</div>
                <el-progress
                  type="circle"
                  :percentage="taskDetail.securityScore"
                  :color="getProgressColor(taskDetail.securityScore)"
                />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="score-item">
                <div class="score-label">性能评分</div>
                <el-progress
                  type="circle"
                  :percentage="taskDetail.performanceScore"
                  :color="getProgressColor(taskDetail.performanceScore)"
                />
              </div>
            </el-col>
          </el-row>
        </div>

        <el-divider />

        <div v-if="reviewResult" class="result-detail">
          <div class="summary-section">
            <h3>审查总结</h3>
            <p>{{ reviewResult.summary }}</p>
          </div>

          <div v-if="reviewResult.issues && reviewResult.issues.length > 0" class="issues-section">
            <h3>发现的问题 (共{{ reviewResult.issues.length }}个)</h3>
            <div v-for="(issue, index) in reviewResult.issues" :key="index" class="issue-item" :class="issue.severity">
              <div class="issue-header">
                <el-tag :type="getSeverityType(issue.severity)" size="small">
                  {{ getSeverityText(issue.severity) }}
                </el-tag>
                <span class="issue-type">{{ issue.type }}</span>
              </div>
              <div class="issue-description">
                <strong>问题描述：</strong>{{ issue.description }}
              </div>
              <div class="issue-suggestion">
                <strong>优化建议：</strong>{{ issue.suggestion }}
              </div>
            </div>
          </div>

          <div v-if="reviewResult.suggestions && reviewResult.suggestions.length > 0" class="suggestions-section">
            <h3>改进建议</h3>
            <ul>
              <li v-for="(suggestion, index) in reviewResult.suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>
        </div>
      </el-card>

      <el-card v-else-if="taskDetail.status === 3" shadow="hover" class="error-card">
        <template #header>
          <span>错误信息</span>
        </template>
        <el-alert
          :title="taskDetail.errorMsg || '审查失败'"
          type="error"
          :closable="false"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getReviewTask } from '@/api/review'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import customParseFormat from 'dayjs/plugin/customParseFormat'
dayjs.extend(customParseFormat)

// Robust date formatter for various backend shapes
const formatDate = (dateTime) => {
  if (dateTime === null || dateTime === undefined) return '-'
  try {
    let d
    if (Array.isArray(dateTime)) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = dateTime
      d = dayjs(new Date(year, (month || 1) - 1, day || 1, hour, minute, second))
    } else if (typeof dateTime === 'number') {
      d = dayjs(dateTime)
    } else if (typeof dateTime === 'string') {
      const normalized = dateTime.includes('T') ? dateTime : dateTime.replace(' ', 'T')
      d = dayjs(normalized)
      if (!d.isValid()) d = dayjs(dateTime, 'YYYY-MM-DD HH:mm:ss')
    } else if (dateTime instanceof Date) {
      d = dayjs(dateTime)
    } else {
      return String(dateTime)
    }
    if (!d || !d.isValid()) return '-'
    return d.format('YYYY-MM-DD HH:mm:ss')
  } catch (e) {
    console.error('formatDate failed:', e, dateTime)
    return '-'
  }
}

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const taskDetail = ref({})
const reviewResult = computed(() => {
  try {
    // 如果 reviewResult 已经是对象，直接返回
    if (typeof taskDetail.value.reviewResult === 'object') {
      return taskDetail.value.reviewResult
    }
    // 如果是字符串，解析后返回
    return JSON.parse(taskDetail.value.reviewResult || '{}')
  } catch (error) {
    console.error('❌ 解析 reviewResult 失败:', error)
    console.error('📄 原始内容:', taskDetail.value.reviewResult)
    return null
  }
})

onMounted(() => {
  fetchTaskDetail()
})

const fetchTaskDetail = async () => {
  const taskId = route.params.id
  if (!taskId) {
    ElMessage.error('任务ID不存在')
    goBack()
    return
  }

  loading.value = true
  try {
    const res = await getReviewTask(taskId)
    taskDetail.value = res.data
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
    goBack()
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const getStatusType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    0: '待审查',
    1: '审查中',
    2: '已完成',
    3: '失败'
  }
  return map[status] || '未知'
}

const getSeverityType = (severity) => {
  const map = {
    'high': 'danger',
    'medium': 'warning',
    'low': 'info'
  }
  return map[severity] || 'info'
}

const getSeverityText = (severity) => {
  const map = {
    'high': '高危',
    'medium': '中等',
    'low': '低危'
  }
  return map[severity] || severity
}

const getProgressColor = (percentage) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

/**
 * 格式化时间
 * 支持多种格式：
 * 1. 数组格式：[2025, 11, 11, 10, 30, 0]
 * 2. 字符串格式：'2025-11-11T10:30:00' 或 '2025-11-11 10:30:00'
 * 3. Date 对象
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'

  try {
    let date

    // 情况1: 数组格式 [year, month, day, hour, minute, second]
    if (Array.isArray(dateTime)) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = dateTime
      date = new Date(year, month - 1, day, hour, minute, second)
    }
    // 情况2: 字符串格式
    else if (typeof dateTime === 'string') {
      date = new Date(dateTime)
    }
    // 情况3: Date 对象
    else if (dateTime instanceof Date) {
      date = dateTime
    }
    // 其他情况
    else {
      return String(dateTime)
    }

    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return '-'
    }

    // 格式化为 YYYY-MM-DD HH:mm:ss
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    const second = String(date.getSeconds()).padStart(2, '0')

    return `${year}-${month}-${day} ${hour}:${minute}:${second}`
  } catch (error) {
    console.error('时间格式化失败:', error, dateTime)
    return '-'
  }
}
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.detail-page :deep(.el-page-header) {
  margin-bottom: 24px;
}

.detail-page :deep(.el-page-header__back) {
  font-weight: 600;
  color: #667eea;
}

.detail-content {
  margin-top: 24px;
}

.info-card,
.code-card,
.result-card,
.error-card {
  margin-bottom: 20px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.info-card:hover,
.code-card:hover,
.result-card:hover,
.error-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.detail-page :deep(.el-card__header) {
  background: linear-gradient(135deg, #f8f9fc 0%, #ffffff 100%);
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 24px;
  font-weight: 600;
  font-size: 16px;
  color: #1a1a2e;
}

.detail-page :deep(.el-card__body) {
  padding: 24px;
}

.detail-page :deep(.el-descriptions) {
  border-radius: 12px;
  overflow: hidden;
}

.detail-page :deep(.el-descriptions__label) {
  font-weight: 600;
  color: #334155;
  background: linear-gradient(135deg, #f8f9fc 0%, #ffffff 100%);
}

.detail-page :deep(.el-descriptions__content) {
  color: #4a5568;
}

.code-content {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  color: #e2e8f0;
  padding: 20px;
  border-radius: 12px;
  overflow-x: auto;
  font-family: 'JetBrains Mono', 'Fira Code', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) inset;
}

.score-section {
  margin: 24px 0;
  padding: 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.03) 0%, rgba(118, 75, 162, 0.03) 100%);
  border-radius: 16px;
}

.score-section h3 {
  margin-bottom: 24px;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  text-align: center;
}

.score-item {
  text-align: center;
}

.score-label {
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #4a5568;
}

.detail-page :deep(.el-progress__text) {
  font-weight: 700;
  font-size: 18px;
}

.result-detail {
  margin-top: 24px;
}

.summary-section {
  margin-bottom: 32px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 16px;
  border-left: 4px solid #667eea;
}

.summary-section h3 {
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.summary-section p {
  line-height: 1.9;
  color: #4a5568;
  font-size: 15px;
}

.issues-section,
.suggestions-section {
  margin-top: 32px;
}

.issues-section h3,
.suggestions-section h3 {
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.issue-item {
  padding: 20px;
  margin-bottom: 16px;
  border-left: 4px solid #4a90e2;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.issue-item:hover {
  transform: translateX(8px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.issue-item.high {
  border-left-color: #f5576c;
  background: linear-gradient(90deg, rgba(245, 87, 108, 0.03) 0%, white 100%);
}

.issue-item.medium {
  border-left-color: #ffd93d;
  background: linear-gradient(90deg, rgba(255, 217, 61, 0.03) 0%, white 100%);
}

.issue-item.low {
  border-left-color: #4facfe;
  background: linear-gradient(90deg, rgba(79, 172, 254, 0.03) 0%, white 100%);
}

.issue-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.issue-type {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 15px;
}

.issue-description,
.issue-suggestion {
  margin-bottom: 12px;
  color: #4a5568;
  line-height: 1.7;
  font-size: 14px;
}

.issue-suggestion {
  color: #667eea;
  font-weight: 500;
}

.suggestions-section ul {
  padding-left: 24px;
}

.suggestions-section li {
  margin-bottom: 12px;
  line-height: 1.8;
  color: #4a5568;
  font-size: 15px;
  position: relative;
}

.suggestions-section li::marker {
  color: #667eea;
  font-weight: bold;
}

.detail-page :deep(.el-tag) {
  border-radius: 8px;
  padding: 6px 14px;
  font-weight: 500;
  border: none;
}

.detail-page :deep(.el-tag--success) {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.detail-page :deep(.el-tag--warning) {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.detail-page :deep(.el-tag--danger) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.detail-page :deep(.el-tag--info) {
  background: linear-gradient(135deg, #a8b5ff 0%, #c4d0fb 100%);
  color: white;
}

.detail-page :deep(.el-alert) {
  border-radius: 12px;
  border: none;
}
</style>
