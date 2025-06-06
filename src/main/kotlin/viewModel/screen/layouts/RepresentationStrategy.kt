package viewModel.screen.layouts

import viewModel.graph.GraphViewModel

interface RepresentationStrategy {
    fun layout(
        height: Double,
        width: Double,
        graphViewModel: GraphViewModel,
    )
}
