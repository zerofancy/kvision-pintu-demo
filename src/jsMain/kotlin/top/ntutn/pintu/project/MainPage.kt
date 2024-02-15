package top.ntutn.pintu.project

import io.kvision.core.Component
import io.kvision.core.Container
import io.kvision.html.button
import io.kvision.panel.responsiveGridPanel

fun Container.mainPage(startClick: () -> Unit, ruleClick: () -> Unit, aboutClick: ()-> Unit): Component =
    responsiveGridPanel {
        options(6, 2, 2, 6, "mt-4") {
            button("开始").onClick {
                startClick()
            }
        }
//        options(6, 3, 2, 6, "mt-4") {
//            button("规则").onClick {
//                ruleClick()
//            }
//        }
        options(6, 4, 2, 6, "mt-4") {
            button("关于").onClick {
                aboutClick()
            }
        }
    }