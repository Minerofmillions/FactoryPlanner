package minerofmillions.utils.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
fun BorderedColumn(
    borderWidth: Dp,
    borderColor: Color,
    borderShape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) = Column(modifier.border(borderWidth, borderColor, borderShape)) {
    Column(Modifier.padding(borderWidth), verticalArrangement, horizontalAlignment, content)
}

@Composable
fun BorderedRow(
    borderWidth: Dp,
    borderColor: Color,
    borderShape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) = Row(modifier.border(borderWidth, borderColor, borderShape)) {
    Row(Modifier.padding(borderWidth), horizontalArrangement, verticalAlignment, content)
}
