package renetik.android.preset

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import renetik.android.core.base.CSTestApplication
import renetik.android.core.kotlin.collections.first
import renetik.android.event.lifecycle.CSModel
import renetik.android.preset.CSPreset.Companion.CSPreset
import renetik.android.preset.model.NotFoundPresetItem
import renetik.android.preset.model.TestCSPresetItemList
import renetik.android.preset.model.defaultCategory
import renetik.android.preset.model.manageItems
import renetik.android.preset.property
import renetik.android.store.reload
import renetik.android.store.type.CSJsonObjectStore

/**
 * A preset written outside the app may quote its scalars or not. The store reads every scalar
 * through its string form, so both documents load identically - the store writes strings only
 * because it must survive a server that changes a value's JSON type without notice.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = CSTestApplication::class)
class PresetJsonValueTypeTest {

    private class Properties(preset: Preset, parent: CSModel) {
        val ticks by preset.property(parent, "ticks", default = 0)
        val stereo by preset.property(parent, "stereo", default = false)
        val mono by preset.property(parent, "mono", default = true)
        val threshold by preset.property(parent, "threshold", default = 0f)
        val title by preset.property(parent, "preset title", default = "")
    }

    private fun loadPropertiesFrom(presetItemJson: String): Properties {
        val parent = CSModel()
        val list = TestCSPresetItemList("PresetItemId")
        list.items(defaultCategory).first!!.store.reload(presetItemJson)
        val preset = CSPreset(
            parent, CSJsonObjectStore(), "test", list, { NotFoundPresetItem() }
        ).manageItems().init()
        return Properties(preset, parent)
    }

    @Test
    fun quotedAndUnquotedScalarsLoadTheSame() {
        val quoted = loadPropertiesFrom(
            """{"ticks":"64","stereo":"true","threshold":"-10.0","preset title":"Hall"}"""
        )
        val unquoted = loadPropertiesFrom(
            """{"ticks":64,"stereo":true,"threshold":-10.0,"preset title":"Hall"}"""
        )

        assertEquals(64, quoted.ticks)
        assertEquals(64, unquoted.ticks)
        assertEquals(true, quoted.stereo)
        assertEquals(true, unquoted.stereo)
        assertEquals(-10.0f, quoted.threshold, 0f)
        assertEquals(-10.0f, unquoted.threshold, 0f)
        assertEquals("Hall", quoted.title)
        assertEquals("Hall", unquoted.title)
    }

    /**
     * What quoting does not rescue: a value has to be written in the form its property parses.
     * An Int property reads its string form with [String.toInt], which rejects a fractional
     * literal, and a Boolean property reads anything that is not `true` as false. Quoting the
     * same literal fails identically - the hazard is the value's form, not its JSON type.
     */
    @Test
    fun valueFormNotParsingAsThePropertyTypeIsLostSilently() {
        assertEquals(0, loadPropertiesFrom("""{"ticks":64.0}""").ticks)
        assertEquals(0, loadPropertiesFrom("""{"ticks":"64.0"}""").ticks)
        assertEquals(false, loadPropertiesFrom("""{"mono":1}""").mono)
        assertEquals(false, loadPropertiesFrom("""{"mono":"1"}""").mono)
    }
}
