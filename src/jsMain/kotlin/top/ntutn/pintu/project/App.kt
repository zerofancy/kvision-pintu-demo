package top.ntutn.pintu.project

import io.kvision.Application
import io.kvision.CoreModule
import io.kvision.BootstrapModule
import io.kvision.BootstrapCssModule
import io.kvision.ToastifyModule
import io.kvision.FontAwesomeModule
import io.kvision.core.*
import io.kvision.form.text.text
import io.kvision.html.*
import io.kvision.module
import io.kvision.panel.*
import io.kvision.routing.Routing
import io.kvision.startApplication
import io.kvision.toast.Toast
import kotlinx.browser.document
import kotlinx.browser.window

class App : Application() {
    override fun start() {
        val routing = Routing.init()
        root("kvapp") {
            vPanel {
                h1("拼图游戏", align = Align.CENTER).onClick {
                    Toast.info("Clicked title")
                    //routing.navigate("/")
                }
                stackPanel {
                    route("/") {
                        mainPage(
                            startClick = { routing.navigate("/start") },
                            ruleClick = { routing.navigate("/rule") },
                            aboutClick = { routing.navigate("/about") }
                        )
                    }
                    route("/start") {
                        gamePage(routing)
                    }
                    route("/rule") {
                    }
                    route("/about") {
                        p("by zerofancy, with <a href=\"https://kvision.gitbook.io/kvision-guide/\" target=\"_blank\">KVision</a>", rich = true)
                    }
                }
            }
        }
    }

}

fun main() {
    startApplication(
        ::App,
        module.hot,
        BootstrapModule,
        BootstrapCssModule,
        ToastifyModule,
        FontAwesomeModule,
        CoreModule
    )
}
