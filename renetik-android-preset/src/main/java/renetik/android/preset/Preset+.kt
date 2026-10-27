package renetik.android.preset

import renetik.android.core.kotlin.changeIf
import renetik.android.core.kotlin.className
import renetik.android.core.lang.Fun
import renetik.android.core.lang.tuples.to
import renetik.android.core.lang.value.isTrue
import renetik.android.event.change.CSHasChange
import renetik.android.event.change.CSHasChange.Companion.action
import renetik.android.event.change.CSHasChangeValue
import renetik.android.event.change.action
import renetik.android.event.change.delegate
import renetik.android.event.change.onChange
import renetik.android.event.change.stateDelegate
import renetik.android.event.dispatch.CSDebouncer.Companion.debouncer
import renetik.android.event.lifecycle.CSHasRegistrationsHasDestruct
import renetik.android.event.lifecycle.onDestructed
import renetik.android.event.property.CSProperty
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.event.property.connect
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistrationsMap
import renetik.android.event.registration.pause
import renetik.android.event.registration.plus
import renetik.android.event.registration.resume
import kotlin.time.Duration.Companion.milliseconds

fun <PresetListItem : CSPresetItem,
        PresetList : CSPresetItemList<PresetListItem>,
        Preset : CSPreset<PresetListItem, PresetList>>
        Preset.init(): Preset = apply {
    if (store.data.isEmpty()) reloadInternal()
}

fun <T : Preset> T.followStoreIf(property: CSProperty<Boolean>) =
    isFollowStore.connect(property)

fun <T : Preset> T.onChange(
    before: Fun,
    onChange: Fun
): CSRegistration = CSRegistration(
    onBeforeChange { before() },
    onChange { onChange() }
)

fun <T : Preset> T.paused(
    vararg registrations: CSRegistration,
    onChange: Fun
): CSRegistration = CSRegistration(
    onBeforeChange { registrations.pause() },
    onChange {
        onChange()
        registrations.resume()
    }
)

fun <T : Preset> T.onChange(
    parent: CSHasRegistrations,
    hasChange: CSHasChange<*>,
    onChange: Fun
): CSRegistration = paused(
    parent + hasChange.onChange { onChange() },
    onChange = { onChange() }
)

fun <T : Preset> T.action(
    parent: CSHasRegistrations,
    hasChange: CSHasChange<*>,
    onChange: Fun
): CSRegistration = paused(
    parent + hasChange.action { onChange() },
    onChange = { onChange() }
)

fun <T> CSHasChangeValue<T>.onChangePausedBy(
    preset: CSPreset<*, *>, function: (T) -> Unit
): CSRegistration {
    val propertyActionRegistration = onChange(function)
    return CSRegistration(propertyActionRegistration, preset.paused(
        propertyActionRegistration, onChange = { function(value) }
    ))
}

fun <T> CSHasChangeValue<T>.actionPausedBy(
    preset: CSPreset<*, *>, function: (T) -> Unit
): CSRegistration {
    val propertyActionRegistration = action(function)
    return CSRegistration(propertyActionRegistration, preset.paused(
        propertyActionRegistration, onChange = { function(value) }
    ))
}

fun CSHasChange<*>.onChangePausedBy(
    preset: CSPreset<*, *>, function: () -> Unit
): CSRegistration {
    val propertyActionRegistration = onChange(function)
    return CSRegistration(propertyActionRegistration, preset.paused(
        propertyActionRegistration, onChange = { function() }
    ))
}

fun CSHasChange<*>.actionPausedBy(
    preset: CSPreset<*, *>, function: () -> Unit
): CSRegistration {
    val propertyActionRegistration = action(function)
    return CSRegistration(propertyActionRegistration, preset.paused(
        propertyActionRegistration, onChange = { function() }
    ))
}

fun <T : Preset> T.onReloadPause(
    vararg registrations: CSRegistration,
): CSRegistration {
    if (isReloadInternal.isTrue) registrations.pause()
    return CSRegistration(
        onBeforeChange { registrations.pause() },
        onChange { registrations.resume() }
    )
}

fun <T : Preset> T.onReloadPause(
    vararg properties: CSProperty<*>,
): CSRegistration {
    if (isReloadInternal.isTrue) properties.forEach(CSProperty<*>::pause)
    return CSRegistration(
        onBeforeChange { properties.forEach(CSProperty<*>::pause) },
        onChange { properties.forEach(CSProperty<*>::resume) }
    )
}

fun <T : Preset> T.onChange(
    paused: CSRegistration,
    before: Fun = {},
    onChange: Fun
): CSRegistration = CSRegistration(
    onBeforeChange {
        paused.pause()
        before()
    }, onChange {
        onChange()
        paused.resume()
    }
)

fun <T : Preset> T.onChange(
    paused: List<CSRegistration>,
    before: Fun = {},
    onChange: Fun
): CSRegistration = CSRegistration(
    onBeforeChange {
        paused.pause()
        before()
    }, onChange {
        onChange()
        paused.resume()
    }
)

fun Preset.isModified(
    parent: CSHasRegistrationsHasDestruct
): CSHasChangeValue<Boolean> = modifiedState(parent) { it }

/**
 * [state] computed with [CSPreset.isModified] after the same debounced changes, so a state that
 * changes while the preset stays modified still updates and never disagrees with [isModified].
 */
fun <T> Preset.modifiedState(
    parent: CSHasRegistrationsHasDestruct, state: (isModified: Boolean) -> T
): CSHasChangeValue<T> {
    val property = property(state(isModified))
    val registration = this + CSRegistrationsMap(className).apply {
        val update = debouncer(500.milliseconds) {
            property.value(state(isModified))
        }
        val storeOnChange = this + store.onChange { update() }
        this + onBeforeChange { storeOnChange.pause() }
        this + onChange { storeOnChange.resume(); update() }
        this + (listItem to listItem.value.store.eventLoaded to eventSave)
            .onChange { update() }
    }
    parent.onDestructed(this, registration::cancel)
    return property
}

fun Preset.itemTitle(
    parent: CSHasRegistrationsHasDestruct, withModified: Boolean = false,
    process: (String) -> String = { it }
): CSHasChangeValue<String> =
    if (withModified) itemTitleWithModified(parent, process)
    else title.delegate(fromValue = { process(it) })

fun Preset.itemTitleWithModified(
    parent: CSHasRegistrationsHasDestruct,
    process: (String) -> String = { it }
): CSHasChangeValue<String> = itemTitleWithModified(isModified(parent), process)

fun Preset.itemTitleWithModified(
    isModified: CSHasChangeValue<Boolean>,
    process: (String) -> String = { it }
): CSHasChangeValue<String> = (itemTitle to isModified).stateDelegate(
    this, fromValues = { title, modified ->
        title.changeIf(String::isNotBlank) {
            "${process(it)}${if (modified) " *" else ""}"
        }
    })

fun Preset.title(
    parent: CSHasRegistrationsHasDestruct, withModified: Boolean = false
): CSHasChangeValue<String> = if (withModified) titleWithModified(parent) else title

fun Preset.titleWithModified(
    parent: CSHasRegistrationsHasDestruct
): CSHasChangeValue<String> = titleWithModified(isModified(parent))

fun Preset.titleWithModified(
    isModified: CSHasChangeValue<Boolean>
): CSHasChangeValue<String> = (title to isModified).stateDelegate(
    this, fromValues = { title, modified ->
        title.changeIf(String::isNotBlank) { "$it${if (modified) " *" else ""}" }
    })