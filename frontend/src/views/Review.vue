<template>
  <div class="review-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>提交代码审查</span>
        </div>
      </template>

      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="reviewForm.title"
            placeholder="请输入审查任务标题"
            clearable
          />
        </el-form-item>

        <el-form-item label="编程语言" prop="language">
          <el-select
            v-model="reviewForm.language"
            placeholder="请选择编程语言"
            style="width: 200px"
          >
            <el-option label="Java" value="Java" />
            <el-option label="Python" value="Python" />
            <el-option label="JavaScript" value="JavaScript" />
            <el-option label="TypeScript" value="TypeScript" />
            <el-option label="C++" value="C++" />
            <el-option label="Go" value="Go" />
            <el-option label="Rust" value="Rust" />
            <el-option label="其他" value="Other" />
          </el-select>
        </el-form-item>

        <el-form-item label="AI模型" prop="aiModel">
          <el-select
            v-model="reviewForm.aiModel"
            placeholder="请选择AI模型"
            style="width: 200px"
          >
            <el-option label="Qwen3-Coder (代码专用)" value="Qwen3-Coder" />
            <el-option label="TBStars2-200B" value="TBStars2-200B-A13B" />
            <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
            <el-option label="GPT-4" value="gpt-4" />
          </el-select>
        </el-form-item>

        <el-form-item label="审查模式" prop="async">
          <el-radio-group v-model="reviewForm.async">
            <el-radio :label="true">异步审查（推荐）</el-radio>
            <el-radio :label="false">同步审查</el-radio>
          </el-radio-group>
          <div style="margin-top: 8px; color: #999; font-size: 12px;">
            异步审查：提交后在后台处理，可在历史记录中查看结果<br/>
            同步审查：立即返回结果，耗时较长
          </div>
        </el-form-item>

        <el-form-item label="代码内容" prop="codeContent">
          <el-input
            v-model="reviewForm.codeContent"
            type="textarea"
            :rows="15"
            placeholder="请粘贴您的代码..."
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            提交审查
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 同步审查结果对话框 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="审查结果"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="reviewResult" class="review-result">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="质量评分">
            <el-tag :type="getScoreType(reviewResult.qualityScore)" size="large">
              {{ reviewResult.qualityScore }}分
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="安全评分">
            <el-tag :type="getScoreType(reviewResult.securityScore)" size="large">
              {{ reviewResult.securityScore }}分
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="性能评分">
            <el-tag :type="getScoreType(reviewResult.performanceScore)" size="large">
              {{ reviewResult.performanceScore }}分
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="result-summary">
          <h3>审查总结</h3>
          <p>{{ reviewResult.summary }}</p>
        </div>

        <div v-if="reviewResult.issues && reviewResult.issues.length > 0" class="issues-section">
          <h3>发现的问题</h3>
          <div v-for="(issue, index) in reviewResult.issues" :key="index" class="issue-item" :class="issue.severity">
            <div class="issue-header">
              <el-tag :type="getSeverityType(issue.severity)" size="small">
                {{ issue.severity }}
              </el-tag>
              <span class="issue-type">{{ issue.type }}</span>
            </div>
            <div class="issue-description">{{ issue.description }}</div>
            <div class="issue-suggestion">
              <strong>建议：</strong>{{ issue.suggestion }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="resultDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToHistory">查看历史</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onActivated, onDeactivated, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { submitReview, syncReview } from '@/api/review'
import { ElMessage, ElMessageBox } from 'element-plus'

// 定义组件名称，供 keep-alive 使用
defineOptions({
  name: 'Review'
})

const router = useRouter()

const reviewFormRef = ref(null)
const loading = ref(false)
const resultDialogVisible = ref(false)
const reviewResult = ref(null)

const reviewForm = reactive({
  title: '',
  codeContent: '',
  language: 'Java',
  aiModel: 'Qwen3-Coder',
  async: true
})

// 生命周期钩子 - 调试用
onMounted(() => {
  console.log('✅ Review 组件已挂载 (mounted)')
})

onUnmounted(() => {
  console.log('❌ Review 组件已卸载 (unmounted)')
})

onActivated(() => {
  console.log('🔄 Review 组件已激活 (activated), 表单数据:', reviewForm)
})

onDeactivated(() => {
  console.log('💤 Review 组件已停用 (deactivated), 表单数据:', reviewForm)
})

const reviewRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 3, max: 200, message: '标题长度为3-200个字符', trigger: 'blur' }
  ],
  language: [
    { required: true, message: '请选择编程语言', trigger: 'change' }
  ],
  aiModel: [
    { required: true, message: '请选择AI模型', trigger: 'change' }
  ],
  codeContent: [
    { required: true, message: '请输入代码内容', trigger: 'blur' },
    { min: 10, message: '代码内容至少10个字符', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!reviewFormRef.value) return

  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (reviewForm.async) {
          // 异步审查
          const res = await submitReview(reviewForm)
          ElMessage.success('任务提交成功，请在审查历史中查看结果')
          setTimeout(() => {
            router.push('/dashboard/history')
          }, 1500)
        } else {
          // 同步审查
          const res = await syncReview(reviewForm)
          console.log('🔍 同步审查响应:', res)
          console.log('📦 响应数据:', res.data)
          console.log('📄 reviewResult 类型:', typeof res.data.reviewResult)
          console.log('📝 reviewResult 内容:', res.data.reviewResult)

          try {
            // 如果 reviewResult 已经是对象，直接使用
            if (typeof res.data.reviewResult === 'object') {
              reviewResult.value = res.data.reviewResult
            } else {
              // 如果是字符串，需要解析
              reviewResult.value = JSON.parse(res.data.reviewResult || '{}')
            }
            resultDialogVisible.value = true
            ElMessage.success('审查完成')
          } catch (parseError) {
            console.error('❌ JSON 解析失败:', parseError)
            console.error('📄 原始内容:', res.data.reviewResult)
            ElMessage.error('解析审查结果失败，请查看控制台日志')
          }
        }
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleReset = () => {
  reviewFormRef.value?.resetFields()
}

const getScoreType = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

const getSeverityType = (severity) => {
  const map = {
    'high': 'danger',
    'medium': 'warning',
    'low': 'info'
  }
  return map[severity] || 'info'
}

const goToHistory = () => {
  resultDialogVisible.value = false
  router.push('/dashboard/history')
}
</script>

<style scoped>
.review-page {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.review-page :deep(.el-card) {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  overflow: hidden;
}

.review-page :deep(.el-card:hover) {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.review-page :deep(.el-card__header) {
  background: linear-gradient(135deg, #f8f9fc 0%, #ffffff 100%);
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 24px;
}

.review-page :deep(.el-card__body) {
  padding: 28px;
}

.review-page :deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
}

.review-page :deep(.el-input__wrapper),
.review-page :deep(.el-textarea__inner) {
  border-radius: 10px;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.review-page :deep(.el-input__wrapper:hover),
.review-page :deep(.el-textarea__inner:hover) {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.review-page :deep(.el-input__wrapper.is-focus),
.review-page :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
}

.review-page :deep(.el-select .el-input__wrapper) {
  border-radius: 10px;
}

.review-page :deep(.el-radio-group) {
  display: flex;
  gap: 16px;
}

.review-page :deep(.el-radio) {
  margin-right: 0;
  padding: 10px 16px;
  border-radius: 10px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.review-page :deep(.el-radio:hover) {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.review-page :deep(.el-radio.is-checked) {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.review-page :deep(.el-button) {
  border-radius: 10px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.review-page :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.review-page :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #7e8fef 0%, #8658b5 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.review-page :deep(.el-button--default) {
  border: 2px solid #e2e8f0;
  background: white;
}

.review-page :deep(.el-button--default:hover) {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.review-result {
  padding: 24px 0;
}

.result-summary {
  margin: 24px 0;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.result-summary h3 {
  margin-bottom: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.result-summary p {
  line-height: 1.8;
  color: #4a5568;
  font-size: 15px;
}

.issues-section {
  margin-top: 32px;
}

.issues-section h3 {
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

.issue-description {
  margin-bottom: 12px;
  color: #4a5568;
  line-height: 1.7;
  font-size: 14px;
}

.issue-suggestion {
  color: #667eea;
  line-height: 1.7;
  font-size: 14px;
  font-weight: 500;
}

.review-page :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.review-page :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
}

.review-page :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

.review-page :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
}

.review-page :deep(.el-descriptions) {
  border-radius: 12px;
  overflow: hidden;
}
</style>
