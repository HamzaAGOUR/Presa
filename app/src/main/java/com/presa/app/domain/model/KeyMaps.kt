package com.presa.app.domain.model

import com.presa.app.R

/** HID keyboard usage ids used by the app. */
object HidKeys {
    const val A = 0x04
    const val B = 0x05
    const val D = 0x07
    const val E = 0x08
    const val F = 0x09
    const val L = 0x0F
    const val P = 0x13
    const val R = 0x15
    const val W = 0x1A

    const val DIGIT_1 = 0x1E
    const val DIGIT_2 = 0x1F
    const val DIGIT_3 = 0x20
    const val DIGIT_4 = 0x21
    const val DIGIT_5 = 0x22
    const val DIGIT_6 = 0x23
    const val DIGIT_7 = 0x24
    const val DIGIT_8 = 0x25
    const val DIGIT_9 = 0x26
    const val DIGIT_0 = 0x27

    const val ENTER = 0x28
    const val ESCAPE = 0x29
    const val BACKSPACE = 0x2A
    const val TAB = 0x2B
    const val SPACE = 0x2C
    const val MINUS = 0x2D
    const val EQUALS = 0x2E

    const val F5 = 0x3E
    const val F11 = 0x44

    const val HOME = 0x4A
    const val PAGE_UP = 0x4B
    const val END = 0x4D
    const val PAGE_DOWN = 0x4E
    const val RIGHT = 0x4F
    const val LEFT = 0x50
    const val DOWN = 0x51
    const val UP = 0x52

    /**
     * Digits are HID usages 0x1E ("1") through 0x26 ("9"), then 0x27 ("0").
     */
    fun digitUsage(digit: Char): Int = when (digit) {
        '1' -> DIGIT_1
        '2' -> DIGIT_2
        '3' -> DIGIT_3
        '4' -> DIGIT_4
        '5' -> DIGIT_5
        '6' -> DIGIT_6
        '7' -> DIGIT_7
        '8' -> DIGIT_8
        '9' -> DIGIT_9
        '0' -> DIGIT_0
        else -> throw IllegalArgumentException("Not a decimal digit: $digit")
    }

    fun digitUsage(digit: Int): Int = digitUsage(digit.toString().first())
}

object VisualizerKeyMap {
    const val PREVIOUS = "visualizer.previous"
    const val NEXT = "visualizer.next"
    const val PLAY_PAUSE = "visualizer.play_pause"
    const val START = "visualizer.start"
    const val END = "visualizer.end"
    const val SLOWER = "visualizer.slower"
    const val FASTER = "visualizer.faster"
    const val ZOOM_IN = "visualizer.zoom_in"
    const val ZOOM_OUT = "visualizer.zoom_out"
    const val FIT = "visualizer.fit"
    const val PANELS = "visualizer.panels"
    const val PRESENTATION = "visualizer.presentation"
    const val RESET = "visualizer.reset"
    const val BACK = "visualizer.back"

    val actions: List<KeyAction> = listOf(
        KeyAction(PREVIOUS, R.string.visualizer_prev, KeyChord.plain(HidKeys.LEFT), repeatable = true),
        KeyAction(NEXT, R.string.visualizer_next, KeyChord.plain(HidKeys.RIGHT), repeatable = true),
        KeyAction(PLAY_PAUSE, R.string.visualizer_play_pause, KeyChord.plain(HidKeys.SPACE)),
        KeyAction(START, R.string.visualizer_start, KeyChord.plain(HidKeys.HOME)),
        KeyAction(END, R.string.visualizer_end, KeyChord.plain(HidKeys.END)),
        KeyAction(SLOWER, R.string.visualizer_slower, KeyChord.plain(HidKeys.DOWN)),
        KeyAction(FASTER, R.string.visualizer_faster, KeyChord.plain(HidKeys.UP)),
        KeyAction(ZOOM_IN, R.string.visualizer_zoom_in, KeyChord.shift(HidKeys.EQUALS)),
        KeyAction(ZOOM_OUT, R.string.visualizer_zoom_out, KeyChord.plain(HidKeys.MINUS)),
        KeyAction(FIT, R.string.visualizer_fit, KeyChord.plain(HidKeys.DIGIT_0)),
        KeyAction(PANELS, R.string.visualizer_panels, KeyChord.plain(HidKeys.D)),
        KeyAction(PRESENTATION, R.string.visualizer_presentation, KeyChord.plain(HidKeys.P)),
        KeyAction(RESET, R.string.visualizer_reset, KeyChord.plain(HidKeys.R)),
        KeyAction(BACK, R.string.visualizer_back, KeyChord.plain(HidKeys.ESCAPE)),
    )

    val byId: Map<String, KeyAction> = actions.associateBy(KeyAction::id)
}

object PowerPointKeyMap {
    const val PREVIOUS = "powerpoint.previous"
    const val NEXT = "powerpoint.next"
    const val FIRST = "powerpoint.first"
    const val LAST = "powerpoint.last"
    const val START_SHOW = "powerpoint.start_show"
    const val FROM_CURRENT = "powerpoint.from_current"
    const val EXIT = "powerpoint.exit"
    const val BLACK = "powerpoint.black"
    const val WHITE = "powerpoint.white"
    const val LASER = "powerpoint.laser"
    const val PEN = "powerpoint.pen"
    const val ERASER = "powerpoint.eraser"

    val actions: List<KeyAction> = listOf(
        KeyAction(PREVIOUS, R.string.slides_prev, KeyChord.plain(HidKeys.LEFT), repeatable = true),
        KeyAction(NEXT, R.string.slides_next, KeyChord.plain(HidKeys.RIGHT), repeatable = true),
        KeyAction(FIRST, R.string.slides_first, KeyChord.plain(HidKeys.HOME)),
        KeyAction(LAST, R.string.slides_last, KeyChord.plain(HidKeys.END)),
        KeyAction(START_SHOW, R.string.slides_start_show, KeyChord.plain(HidKeys.F5)),
        KeyAction(FROM_CURRENT, R.string.slides_from_current, KeyChord.shift(HidKeys.F5)),
        KeyAction(EXIT, R.string.slides_exit, KeyChord.plain(HidKeys.ESCAPE)),
        KeyAction(BLACK, R.string.slides_black, KeyChord.plain(HidKeys.B)),
        KeyAction(WHITE, R.string.slides_white, KeyChord.plain(HidKeys.W)),
        KeyAction(LASER, R.string.slides_laser, KeyChord.ctrl(HidKeys.L)),
        KeyAction(PEN, R.string.slides_pen, KeyChord.ctrl(HidKeys.P)),
        KeyAction(ERASER, R.string.slides_eraser, KeyChord.plain(HidKeys.E)),
    )

    val byId: Map<String, KeyAction> = actions.associateBy(KeyAction::id)
}

object PdfKeyMap {
    const val PREVIOUS_PAGE = "pdf.previous_page"
    const val NEXT_PAGE = "pdf.next_page"
    const val FIRST = "pdf.first"
    const val LAST = "pdf.last"
    const val SCROLL_UP = "pdf.scroll_up"
    const val SCROLL_DOWN = "pdf.scroll_down"
    const val FULLSCREEN = "pdf.fullscreen"
    const val FULLSCREEN_ALT = "pdf.fullscreen_alt"
    const val ZOOM_IN = "pdf.zoom_in"
    const val ZOOM_OUT = "pdf.zoom_out"
    const val FIT = "pdf.fit"
    const val EXIT = "pdf.exit"

    val actions: List<KeyAction> = listOf(
        KeyAction(
            PREVIOUS_PAGE,
            R.string.slides_pdf_prev_page,
            KeyChord.plain(HidKeys.PAGE_UP),
            repeatable = true,
        ),
        KeyAction(
            NEXT_PAGE,
            R.string.slides_pdf_next_page,
            KeyChord.plain(HidKeys.PAGE_DOWN),
            repeatable = true,
        ),
        KeyAction(FIRST, R.string.slides_first, KeyChord.plain(HidKeys.HOME)),
        KeyAction(LAST, R.string.slides_last, KeyChord.plain(HidKeys.END)),
        KeyAction(SCROLL_UP, R.string.slides_pdf_scroll_up, KeyChord.plain(HidKeys.UP)),
        KeyAction(SCROLL_DOWN, R.string.slides_pdf_scroll_down, KeyChord.plain(HidKeys.DOWN)),
        KeyAction(FULLSCREEN, R.string.slides_pdf_fullscreen, KeyChord.plain(HidKeys.F11)),
        KeyAction(FULLSCREEN_ALT, R.string.slides_pdf_fullscreen_alt, KeyChord.ctrl(HidKeys.L)),
        KeyAction(ZOOM_IN, R.string.slides_pdf_zoom_in, KeyChord.ctrlShift(HidKeys.EQUALS)),
        KeyAction(ZOOM_OUT, R.string.slides_pdf_zoom_out, KeyChord.ctrl(HidKeys.MINUS)),
        KeyAction(FIT, R.string.slides_pdf_fit, KeyChord.ctrl(HidKeys.DIGIT_0)),
        KeyAction(EXIT, R.string.slides_exit, KeyChord.plain(HidKeys.ESCAPE)),
    )

    val byId: Map<String, KeyAction> = actions.associateBy(KeyAction::id)
}

object KeyMaps {
    val allActions: List<KeyAction> =
        VisualizerKeyMap.actions + PowerPointKeyMap.actions + PdfKeyMap.actions

    private val actionsById: Map<String, KeyAction> =
        allActions.associateBy(KeyAction::id)

    fun find(actionId: String): KeyAction? = actionsById[actionId]

    fun actionsFor(mode: AppMode, subMode: SlidesSubMode): List<KeyAction> = when (mode) {
        AppMode.VISUALIZER -> VisualizerKeyMap.actions
        AppMode.SLIDES -> when (subMode) {
            SlidesSubMode.POWERPOINT -> PowerPointKeyMap.actions
            SlidesSubMode.PDF -> PdfKeyMap.actions
        }
    }

    fun effectiveChord(action: KeyAction, overrides: Map<String, KeyChord>): KeyChord =
        overrides[action.id] ?: action.defaultChord

    fun digitChord(digit: Char): KeyChord = KeyChord.plain(HidKeys.digitUsage(digit))
}
