package minerofmillions.recipe_factory.core.config

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.getValue
import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.InMemoryFormat
import com.electronwill.nightconfig.core.UnmodifiableConfig
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class FactoryPlannerPluginSpec private constructor(
    values: UnmodifiableConfig
) : UnmodifiableConfig by values {
    private var isCorrecting = false
    private val childConfig: Config = Config.inMemory()

    class Builder {
        internal val values = mutableListOf<ConfigValue<*>>()

        fun define(path: String, defaultValue: String): ConfigValue<String> =
            StringValue(this, path, defaultValue).also(values::add)

        fun define(path: String, defaultValue: Double): ConfigValue<Double> =
            DoubleValue(this, path, defaultValue).also(values::add)

        fun define(path: String, defaultValue: Int): ConfigValue<Int> =
            IntValue(this, path, defaultValue).also(values::add)

        fun define(path: String, defaultValue: Boolean): ConfigValue<Boolean> =
            BooleanValue(this, path, defaultValue).also(values::add)

        fun build(): FactoryPlannerPluginSpec {
            val valueCfg = Config.of(
                Config.getDefaultMapCreator(true, true),
                InMemoryFormat.withSupport(ConfigValue::class.java::isAssignableFrom)
            )
            values.forEach { valueCfg.set(it.path, it) }

            val ret = FactoryPlannerPluginSpec(valueCfg)
            values.forEach { it.spec = ret }

            return ret
        }
    }

    sealed class ConfigValue<T : Any>(
        parent: Builder,
        val path: String,
        private val defaultValue: T,
        val klass: KClass<T>
    ) : Value<T>() {
        private val _value = MutableValue(defaultValue)
        internal lateinit var spec: FactoryPlannerPluginSpec

        override val value: T by _value

        override fun subscribe(observer: (T) -> Unit) {
            _value.subscribe(observer)
        }

        override fun unsubscribe(observer: (T) -> Unit) {
            _value.unsubscribe(observer)
        }

        override fun toString() = _value.value.toString()

        init {
            parent.values.add(this)
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = _value.getValue(thisRef, property)
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            _value.value = value
        }

        fun set(value: T) {
            _value.value = value
        }

        private fun getRaw(config: Config, path: String): T =
            config[path] ?: defaultValue
    }

    class StringValue(parent: Builder, path: String, defaultValue: String) :
        ConfigValue<String>(parent, path, defaultValue, String::class)

    class DoubleValue(parent: Builder, path: String, defaultValue: Double) :
        ConfigValue<Double>(parent, path, defaultValue, Double::class)

    class IntValue(parent: Builder, path: String, defaultValue: Int) :
        ConfigValue<Int>(parent, path, defaultValue, Int::class)

    class BooleanValue(parent: Builder, path: String, defaultValue: Boolean) :
        ConfigValue<Boolean>(parent, path, defaultValue, Boolean::class)

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}