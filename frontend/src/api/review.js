import request from '@/utils/request'

// 提交代码审查（异步）
export function submitReview(data) {
  return request({
    url: '/api/review/submit',
    method: 'post',
    data
  })
}

// 同步代码审查
export function syncReview(data) {
  return request({
    url: '/api/review/sync',
    method: 'post',
    data
  })
}

// 查询审查任务详情
export function getReviewTask(taskId) {
  return request({
    url: `/api/review/task/${taskId}`,
    method: 'get'
  })
}

// 获取审查任务列表
export function getReviewTasks(params) {
  return request({
    url: '/api/review/tasks',
    method: 'get',
    params
  })
}

// 删除审查任务
export function deleteReviewTask(taskId) {
  return request({
    url: `/api/review/task/${taskId}`,
    method: 'delete'
  })
}
