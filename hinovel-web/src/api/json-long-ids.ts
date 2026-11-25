/**
 * Serialize Long IDs received as decimal strings without rounding them through Number.
 */
export function stringifyWithLongIds(body: object, idFields: readonly string[]): string {
    const fields: string[] = []
    for (const [key, value] of Object.entries(body)) {
        const json = idFields.includes(key) && typeof value === 'string' && /^-?\d+$/.test(value)
            ? BigInt(value).toString()
            : JSON.stringify(value)
        if (json !== undefined) fields.push(`${JSON.stringify(key)}:${json}`)
    }
    return `{${fields.join(',')}}`
}
