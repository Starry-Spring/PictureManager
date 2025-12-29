// src/types/image.ts
export interface ImageResponseDTO {
    id: number
    title: string
    description: string
    originalFilename: string
    storedFilename: string
    filePath: string
    fileSize: number
    mimeType: string
    width: number
    height: number
    tags: string[]
    uploadedAt: string

    // EXIF信息
    cameraMake?: string
    cameraModel?: string
    takenAt?: string
    exposureTime?: string
    fNumber?: string
    isoSpeed?: number
    focalLength?: string
    latitude?: number
    longitude?: number
}