<template>
  <div class="history-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>审查历史</span>
          <el-button type="primary" size="small" @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <div class="filter-bar">
        <el-select
          v-model="queryParams.status"
          placeholder="审查状态"
          clearable
          style="width: 150px"
          @change="handleQuery"
        >
          <el-option label="全部" :value="null" />
          <el-option label="待审查" :value="0" />
          <el-option label="审查中" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="失败" :value="3" />
        </el-select>

        <el-select
          v-model="queryParams.language"
          placeholder="编程语言"
          clearable
          style="width: 150px"
          @change="handleQuery"
        >
          <el-option label="Java" value="Java" />
          <el-option label="Python" value="Python" />
          <el-option label="JavaScript" value="JavaScript" />
        </el-select>

        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 任务列表 -->
      <el-table
        v-loading="loading"
        :data="taskList"
        stripe
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="language" label="语言" width="100" />
        <el-table-column prop="aiModel" label="AI模型" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="120">
          <template #default="{ row }">
            <span v-if="row.qualityScore !== null">
              <el-tag :type="getScoreType(row.qualityScore)" size="small">
                {{ row.qualityScore }}分
              </el-tag>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 2"
              type="primary"
              size="small"
              @click="viewDetail(row.id)"
            >
              查看详情
            </el-button>
            <el-button
              v-else-if="row.status === 1"
              type="info"
              size="small"
              disabled
            >
              审查中...
            </el-button>
            <el-button
              v-else-if="row.status === 0"
              type="warning"
              size="small"
              disabled
            >
              等待中...
            </el-button>
            <el-button
              v-else
              type="danger"
              size="small"
            >
              失败
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onActivated, onDeactivated, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getReviewTasks, deleteReviewTask } from '@/api/review'
import { ElMessage, ElMessageBox } from 'element-plus'

// 定义组件名称，供 keep-alive 使用
defineOptions({
  name: 'History'
})

const router = useRouter()

const loading = ref(false)
const taskList = ref([])
const total = ref(0)
const isFirstLoad = ref(true)

const queryParams = reactive({
  page: 1,
  size: 10,
  status: null,
  language: null
})

// 生命周期钩子 - 调试用
onMounted(() => {
  console.log('✅ History 组件已挂载 (mounted)')
})

onUnmounted(() => {
  console.log('❌ History 组件已卸载 (unmounted)')
})

// 使用 onActivated 替代 onMounted，这样组件被缓存后重新激活时也会触发
// 但首次激活后不会重复加载，除非用户主动刷新
onActivated(() => {
  console.log('🔄 History 组件已激活 (activated), 查询参数:', queryParams)
  if (isFirstLoad.value) {
    fetchTaskList()
    isFirstLoad.value = false
  }
})

onDeactivated(() => {
  console.log('💤 History 组件已停用 (deactivated), 查询参数:', queryParams)
})

const fetchTaskList = async () => {
  loading.value = true
  try {
    const res = await getReviewTasks(queryParams)
    taskList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.page = 1
  fetchTaskList()
}

const handleReset = () => {
  queryParams.status = null
  queryParams.language = null
  queryParams.page = 1
  fetchTaskList()
}

const refreshList = () => {
  isFirstLoad.value = false  // 确保刷新时不受首次加载标记影响
  fetchTaskList()
  ElMessage.success('刷新成功')
}

const viewDetail = (id) => {
  router.push(`/dashboard/detail/${id}`)
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条审查记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteReviewTask(id)
      ElMessage.success('删除成功')
      fetchTaskList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
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

const getScoreType = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
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
.history-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.history-page :deep(.el-card) {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.history-page :deep(.el-card__header) {
  background: linear-gradient(135deg, #f8f9fc 0%, #ffffff 100%);
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 24px;
}

.history-page :deep(.el-card__body) {
  padding: 24px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-bar :deep(.el-select),
.filter-bar :deep(.el-input) {
  min-width: 180px;
}

.filter-bar :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.filter-bar :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.filter-bar :deep(.el-button) {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.filter-bar :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.filter-bar :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #7e8fef 0%, #8658b5 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.filter-bar :deep(.el-button--default) {
  border: 2px solid #e2e8f0;
}

.filter-bar :deep(.el-button--default:hover) {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.history-page :deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
  font-size: 14px;
}

.history-page :deep(.el-table thead) {
  background: linear-gradient(135deg, #f8f9fc 0%, #ffffff 100%);
}

.history-page :deep(.el-table th) {
  background: transparent;
  color: #334155;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e2e8f0;
}

.history-page :deep(.el-table td) {
  border-bottom: 1px solid #f1f5f9;
}

.history-page :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.history-page :deep(.el-table__row:hover) {
  background: linear-gradient(90deg, rgba(102, 126, 234, 0.03) 0%, transparent 100%);
  transform: scale(1.01);
}

.history-page :deep(.el-table__empty-text) {
  padding: 60px 0;
  color: #94a3b8;
  font-size: 15px;
}

.history-page :deep(.el-tag) {
  border-radius: 8px;
  padding: 4px 12px;
  font-weight: 500;
  border: none;
}

.history-page :deep(.el-tag--success) {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.history-page :deep(.el-tag--warning) {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.history-page :deep(.el-tag--danger) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.history-page :deep(.el-tag--info) {
  background: linear-gradient(135deg, #a8b5ff 0%, #c4d0fb 100%);
  color: white;
}

.history-page :deep(.el-button--small) {
  border-radius: 8px;
  padding: 6px 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.history-page :deep(.el-button--primary.el-button--small) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.history-page :deep(.el-button--primary.el-button--small:hover) {
  background: linear-gradient(135deg, #7e8fef 0%, #8658b5 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.history-page :deep(.el-button--danger.el-button--small) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  color: white;
}

.history-page :deep(.el-button--danger.el-button--small:hover) {
  background: linear-gradient(135deg, #f5a3ff 0%, #f7677c 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

.history-page :deep(.el-button--info.el-button--small),
.history-page :deep(.el-button--warning.el-button--small) {
  border: none;
  color: white;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.pagination :deep(.el-pagination) {
  gap: 8px;
}

.pagination :deep(.el-pager li) {
  border-radius: 8px;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.pagination :deep(.el-pager li:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}

.pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next) {
  border-radius: 8px;
  width: 36px;
  height: 36px;
  transition: all 0.3s ease;
}

.pagination :deep(.btn-prev:hover),
.pagination :deep(.btn-next:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}
</style>
