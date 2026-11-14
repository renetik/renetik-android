package renetik.android.core.android.text

import android.text.Html
import android.text.Spanned

/** This text with its line breaks and `[B]`…`[/B]` bold marks rendered. */
fun String.parseFormatted(): Spanned = Html.fromHtml(
    replace("\n", "<br>").replace("[B]", "<b>").replace("[/B]", "</b>"), 0
)
