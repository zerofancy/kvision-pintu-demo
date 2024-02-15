package top.ntutn.pintu.project

import io.kvision.core.*
import io.kvision.html.Div
import io.kvision.html.div
import io.kvision.panel.gridPanel
import io.kvision.panel.simplePanel
import io.kvision.panel.splitPanel
import io.kvision.redux.RAction
import io.kvision.redux.createReduxStore
import io.kvision.routing.Routing
import io.kvision.state.bind
import io.kvision.toast.Toast
import io.kvision.utils.perc
import io.kvision.utils.pt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

data class GameStatus(val width: Int, val height: Int, val data: List<Int>) {
    companion object {
        fun newGame(width: Int, height: Int): GameStatus {
            var initList = (1..< width * height).toList() + 0
            var blankRow = height - 1
            var blankCol = width - 1
            val offsetArray = listOf(-1, 0, 1)
            repeat(width * height * 100) {
                val newRow = blankRow + offsetArray.random()
                val newCol = blankCol + offsetArray.random()
                if ((newRow == blankRow || newCol == blankCol) && newRow in 0..< height && newCol in 0..< width) {
                    val oldIndex = blankRow * width + blankCol
                    val newIndex = newRow * width + newCol
                    initList = initList.swap(oldIndex, newIndex)
                    blankRow = newRow
                    blankCol = newCol
                }
            }
            return GameStatus(width, height, initList)
        }
    }

    val blankIndex get() = data.indexOf(0)

    fun getDataAt(col: Int, row: Int) = data[row * width + col]

    fun dragValid(col: Int, row: Int): Boolean {
        val (blankCol, blankRow) = blankIndex % width to blankIndex / width
        return (blankCol == col && abs(blankRow - row) == 1)
                || (abs(blankCol - col) == 1 && blankRow == row)
    }

    fun success() : Boolean {
        data.forEachIndexed { index, i ->
            var tmp = data[index]
            if (tmp == 0) {
                tmp = width * height // 空格在最后一个位置
            }
            if (index + 1 != tmp) {
                return false
            }
        }
        return true
    }
}

sealed class GameAction: RAction {
    @Serializable
    data class Drag(@SerialName("col") val col: Int, @SerialName("row") val row: Int): GameAction()

    data class NewGame(val width: Int, val height: Int): GameAction()
}

private fun <T> List<T>.swap(i: Int, j: Int): List<T> {
    val newList = toMutableList()
    newList[i] = get(j)
    newList[j] = get(i)
    return newList
}

fun gameReducer(state: GameStatus, action: GameAction): GameStatus = when (action) {
    is GameAction.Drag -> {
        val targetIndex = action.row * state.width + action.col
        state.copy(data = state.data.swap(state.blankIndex, targetIndex))
    }
    is GameAction.NewGame -> {
        GameStatus.newGame(state.width, state.height)
    }
}

fun Container.gamePage(routing: Routing): Component {
    val store = createReduxStore(::gameReducer, GameStatus.newGame(3, 3))
    return splitPanel {
        simplePanel().bind(store) { state ->
            width = 80.perc
            val succeeded = state.success()
            gridPanel(columnGap = state.width, rowGap = state.height, justifyContent = JustifyContent.CENTER) {
                val initCell: Div.(col: Int, row: Int) -> Unit = { col, row ->
                    val data = state.getDataAt(col, row)
                    content = if (data == 0 && !succeeded) {
                        ""
                    } else {
                        data.toString()
                    }

                    width = 100.pt
                    height = 100.pt
                    background = if (data == 0) {
                        Background(Color.name(Col.GRAY))
                    } else {
                        Background(Color.name(Col.LIGHTGRAY))
                    }
                    setDragDropData("application/json",
                        //Json.encodeToString(GameAction.Drag(col, row))
                        "{}"
                    )

                    draggable = data == 0
                    if (state.dragValid(col, row) && !succeeded) {
                        setDropTarget("application/json") {
//                            val dragString = it.dataTransfer?.getData("application/json") ?: return@setDropTarget
//                            val dragData = runCatching {
//                                Json.decodeFromString<GameAction.Drag>(dragString)
//                            }.getOrNull() ?: return@setDropTarget
                            store.dispatch(GameAction.Drag(col, row))
                        }
                        onClick {
                            store.dispatch(GameAction.Drag(col, row))
                        }
                    }
                }

                var counter = 0
                for (i in 0 until state.height) {
                    for (j in 0 until state.width) {
                        counter++
                        options(j + 1, i + 1) {
                            div(counter.toString()) {
                                initCell(j, i)
                            }
                        }
                    }
                }
            }
            if (succeeded) {
                Toast.success("恭喜通关！3s后返回首页。")
                GlobalScope.launch {
                    delay(3_000)
                    store.dispatch(GameAction.NewGame(3, 3))
                    routing.navigate("/")
                }
            }
        }
        div("你可以点击白块旁边的方块，或者将白块拖动到周围的方块上。")
    }
}