import http from './http'

export interface NovelCharacter {
    id: number
    novelId: number
    name: string
    alias: string
    avatarUrl: string
    gender: 'MALE' | 'FEMALE' | 'OTHER'
    age: number
    appearance: string
    personality: string
    background: string
    goals: string
    abilities: string
    notes: string
    roleType: string
    color: string
    identity: string
    sortOrder: number
    createTime: string
    updateTime: string
}

export interface NovelCharacterRelation {
    id: number
    novelId: number
    characterId: number
    targetId: number
    relationType: string
    description: string
    createTime: string
    updateTime: string
}

export interface CharacterGraph {
    characters: NovelCharacter[]
    relations: NovelCharacterRelation[]
}

export interface CreateCharacterRequest {
    novelId: number
    name: string
    alias?: string
    avatarUrl?: string
    gender?: string
    age?: number
    appearance?: string
    personality?: string
    background?: string
    goals?: string
    abilities?: string
    notes?: string
    roleType?: string
    color?: string
    identity?: string
}

export interface UpdateCharacterRequest {
    name?: string
    alias?: string
    avatarUrl?: string
    gender?: string
    age?: number
    appearance?: string
    personality?: string
    background?: string
    goals?: string
    abilities?: string
    notes?: string
    roleType?: string
    color?: string
    identity?: string
}

export interface CreateRelationRequest {
    novelId: number
    characterId: number
    targetId: number
    relationType: string
    description?: string
}

// 关系类型选项
export const relationTypeOptions = [
    {label: '朋友', value: 'FRIEND', color: '#67C23A'},
    {label: '敌人', value: 'ENEMY', color: '#F56C6C'},
    {label: '恋人', value: 'LOVER', color: '#E6A23C'},
    {label: '家人', value: 'FAMILY', color: '#409EFF'},
    {label: '师徒', value: 'MASTER', color: '#909399'},
    {label: '同事', value: 'COLLEAGUE', color: '#8E44AD'},
    {label: '其他', value: 'OTHER', color: '#606266'},
]

// 角色类型选项
export const roleTypeOptions = [
    {label: '主角', value: 'PROTAGONIST', color: '#ff6b6b'},
    {label: '配角', value: 'SUPPORTING', color: '#4facfe'},
    {label: '反派', value: 'ANTAGONIST', color: '#f59e0b'},
    {label: '其他', value: 'OTHER', color: '#9aa0a6'},
]

// ==================== 人物管理 ====================

export const createCharacter = (data: CreateCharacterRequest) =>
    http.post<never, { data: NovelCharacter }>('/api/novel/character/characters', data)

export const updateCharacter = (characterId: number, data: UpdateCharacterRequest) =>
    http.put<never, { data: NovelCharacter }>(`/api/novel/character/characters/${characterId}`, data)

export const deleteCharacter = (characterId: number) =>
    http.delete<never, { data: void }>(`/api/novel/character/characters/${characterId}`)

// ==================== 关系管理 ====================

export const createRelation = (data: CreateRelationRequest) =>
    http.post<never, { data: NovelCharacterRelation }>('/api/novel/character/character-relations', data)

export const deleteRelation = (relationId: number) =>
    http.delete<never, { data: void }>(`/api/novel/character/character-relations/${relationId}`)

export const listCharacterRelations = (characterId: number) =>
    http.get<never, { data: NovelCharacterRelation[] }>(`/api/novel/character/characters/${characterId}/relations`)

export const getCharacterGraph = (novelId: number) =>
    http.get<never, { data: CharacterGraph }>(`/api/novel/character/novels/${novelId}/character-graph`)
