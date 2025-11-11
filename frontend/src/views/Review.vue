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
  font-size: 18px;
  font-weight: 500;
}

.review-result {
  padding: 20px 0;
}

.result-summary {
  margin: 20px 0;
}

.result-summary h3 {
  margin-bottom: 10px;
  font-size: 16px;
}

.result-summary p {
  line-height: 1.6;
  color: #666;
}

.issues-section {
  margin-top: 30px;
}

.issues-section h3 {
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

.issue-description {
  margin-bottom: 10px;
  color: #666;
  line-height: 1.6;
}

.issue-suggestion {
  color: #409eff;
  line-height: 1.6;
}
</style>
