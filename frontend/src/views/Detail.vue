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
            {{ formatDateTime(taskDetail.createTime) }}
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
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.detail-content {
  margin-top: 20px;
}

.info-card,
.code-card,
.result-card,
.error-card {
  margin-bottom: 20px;
}

.code-content {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.score-section {
  margin: 20px 0;
}

.score-section h3 {
  margin-bottom: 20px;
  font-size: 16px;
}

.score-item {
  text-align: center;
}

.score-label {
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.result-detail {
  margin-top: 20px;
}

.summary-section {
  margin-bottom: 30px;
}

.summary-section h3 {
  margin-bottom: 10px;
  font-size: 16px;
}

.summary-section p {
  line-height: 1.8;
  color: #666;
}

.issues-section,
.suggestions-section {
  margin-top: 30px;
}

.issues-section h3,
.suggestions-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
}

.issue-item {
  padding: 15px;
  margin-bottom: 15px;
  border-left: 3px solid #409eff;
  background: #f5f7fa;
  border-radius: 4px;
}

.issue-item.high {
  border-left-color: #f56c6c;
  background: #fef0f0;
}

.issue-item.medium {
  border-left-color: #e6a23c;
  background: #fdf6ec;
}

.issue-item.low {
  border-left-color: #67c23a;
  background: #f0f9ff;
}

.issue-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.issue-type {
  font-weight: 500;
  color: #333;
}

.issue-description,
.issue-suggestion {
  margin-bottom: 10px;
  color: #666;
  line-height: 1.6;
}

.suggestions-section ul {
  padding-left: 20px;
}

.suggestions-section li {
  margin-bottom: 10px;
  line-height: 1.6;
  color: #666;
}
</style>
