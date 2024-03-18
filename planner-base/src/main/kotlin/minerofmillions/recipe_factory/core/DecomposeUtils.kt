package minerofmillions.recipe_factory.core

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import mu.KotlinLogging

private val logger = KotlinLogging.logger {  }

fun ComponentContext.componentCoroutineScope(): CoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    if (lifecycle.state != Lifecycle.State.DESTROYED) lifecycle.doOnDestroy(scope::cancel) else scope.cancel()

    return scope
}
