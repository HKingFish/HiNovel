import http from './http'

export async function uploadImage(file: File): Promise<string> {
    const body = new FormData()
    body.append('file', file)
    const response = await http.post<never, { data: { url: string } }>('/api/infra/oss/upload', body, {
        headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data.url
}
