import { init, use } from 'echarts/core'
import { GraphChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([GraphChart, TooltipComponent, CanvasRenderer])

export { init as initCharacterChart }
export type { ECharts } from 'echarts/core'
export type { EChartsOption } from 'echarts'
