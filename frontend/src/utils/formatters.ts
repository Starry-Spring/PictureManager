// src/utils/formatters.ts
export const formatDate = (dateString?: string): string => {
    if (!dateString) return '未知时间'

    try {
        const date = new Date(dateString)
        const now = new Date()
        const diffInMs = now.getTime() - date.getTime()
        const diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24))

        if (diffInDays === 0) {
            // 今天
            return date.toLocaleTimeString('zh-CN', {
                hour: '2-digit',
                minute: '2-digit'
            })
        } else if (diffInDays === 1) {
            // 昨天
            return '昨天 ' + date.toLocaleTimeString('zh-CN', {
                hour: '2-digit',
                minute: '2-digit'
            })
        } else if (diffInDays < 7) {
            // 一周内
            return `${diffInDays}天前`
        } else {
            // 更早
            return date.toLocaleDateString('zh-CN')
        }
    } catch {
        return '未知时间'
    }
}

export const formatFileSize = (bytes?: number): string => {
    if (!bytes) return '0 B'

    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    let size = bytes
    let unitIndex = 0

    while (size >= 1024 && unitIndex < units.length - 1) {
        size /= 1024
        unitIndex++
    }

    return `${size.toFixed(1)} ${units[unitIndex]}`
}