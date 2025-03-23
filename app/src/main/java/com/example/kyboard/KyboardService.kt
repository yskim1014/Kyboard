package com.example.kyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.view.MotionEvent
import android.util.Log

class KyboardService : InputMethodService() {
    private enum class InputMode { CHO, JUNG, JONG }
    private val hangulAutomata = HangulAutomata()
    private var currentMode = InputMode.CHO
    private var startX = 0f
    private var startY = 0f
    private var layoutMode: String = "Korean"  // 모드 구분

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_korean_sebeol, null)
        setupButtonListeners(keyboardView)
        return keyboardView
    }

    private fun getButtonIds(): List<Int> {
        val excludeIds = setOf(
            R.id.key_01, R.id.key_02, R.id.key_03, R.id.key_04, R.id.key_05,
            R.id.key_06, R.id.key_07, R.id.key_08, R.id.key_09, R.id.key_10,
            R.id.key_11, R.id.key_12
        )

        return R.id::class.java.fields
            .filter { it.name.startsWith("key_") }  // "key_"로 시작하는 ID만 필터링
            .mapNotNull { it.getInt(null) }         // 정수 ID 값 가져오기
            .filterNot { it in excludeIds }         // 제외할 ID 제거
    }

    private fun setupButtonListeners(keyboardView: View) {
        val buttonsClick = getButtonIds()  // 자동으로 key_01 ~ key_12를 제외한 ID 가져오기
        val buttonsTouch = listOf(
            R.id.key_01, R.id.key_02, R.id.key_03, R.id.key_04, R.id.key_05,
            R.id.key_06, R.id.key_07, R.id.key_08, R.id.key_09, R.id.key_10,
            R.id.key_11, R.id.key_12
        )

        buttonsClick.forEach { id ->
            keyboardView.findViewById<Button>(id)?.setOnClickListener { handleClick(it) }
        }

        buttonsTouch.forEach { id ->
            keyboardView.findViewById<Button>(id)?.setOnTouchListener { v, event -> handleTouch(v, event) }
        }
    }

    // 터치 이벤트 핸들링
    private fun handleTouch(v: View, event: MotionEvent): Boolean {
        val keyText = (v as Button).text.toString()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                val combinedText = hangulAutomata.inputChar(keyText[0])
                currentInputConnection.setComposingText(combinedText, 1)
                return true
            }

            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - startX
                val deltaY = event.y - startY
                when (currentMode) {
                    InputMode.CHO -> handleCho(keyText)  // 초성 입력 처리
                    InputMode.JUNG -> handleJung(keyText)  // 중성 입력 처리
                    InputMode.JONG -> handleJong(keyText)  // 종성 입력 처리
                }
                if (Math.abs(deltaX) < 5 && Math.abs(deltaY) < 5) {
                    handleKeyPress(keyText)  // 기본 키 입력
                }
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // 좌/우 드래그
                    if (deltaX > 50) handleSwipeRight(keyText)
                    else if (deltaX < -50) handleSwipeLeft(keyText)
                } else {
                    // 상/하 드래그
                    if (deltaY > 50) handleSwipeDown(keyText)
                    else if (deltaY < -50) handleSwipeUp(keyText)
                }
                return true
            }
        }
        return false
    }

    private fun handleSwipeLeft(keyText: String) {
        val ic = currentInputConnection ?: return
        val newText = when (keyText) {
            "ㄱ" -> "ㄲ"
            "ㄷ" -> "ㄸ"
            "ㅂ" -> "ㅃ"
            "ㅅ" -> "ㅆ"
            "ㅈ" -> "ㅉ"
            else -> keyText
        }
        ic.commitText(newText, 1)
    }

    private fun handleSwipeRight(keyText: String) {
        val ic = currentInputConnection ?: return
        val newText = when (keyText) {
            "ㅇ" -> "ㅎ"
            "ㄱ" -> "ㅋ"
            "ㄴ" -> "ㅁ"
            "ㄷ" -> "ㅌ"
            "ㅂ" -> "ㅍ"
            "ㅈ" -> "ㅊ"
            else -> keyText
        }
        ic.commitText(newText, 1)
    }

    private fun handleSwipeUp(keyText: String) {
        val ic = currentInputConnection ?: return
        val newText = when (keyText) {
            "ㅇ" -> "1"
            "ㄱ" -> "2"
            "ㅏ" -> "3"
            "ㄴ" -> "4"
            "ㄷ" -> "5"
            "ㅓ" -> "6"
            "ㅁ" -> "7"
            "ㅂ" -> "8"
            "ㅣ" -> "9"
            "ㅅ" -> "*"
            "ㅈ" -> "0"
            "ㅡ" -> "#"
            else -> keyText
        }
        ic.commitText(newText, 1)
    }

    private fun handleSwipeDown(keyText: String) {
        val ic = currentInputConnection ?: return
        val newText = when (keyText) {
            "ㅇ" -> "ㅎ"
            "ㄱ" -> "ㅋ"
            "ㄴ" -> "ㅁ"
            "ㄷ" -> "ㅌ"
            "ㅂ" -> "ㅠ"
            "ㅈ" -> "ㅊ"
            else -> keyText
        }
        ic.commitText(newText, 1)
    }

    private fun handleCho(key: String) {
        // 초성 입력 처리 로직
        currentMode = InputMode.JUNG  // 중성 모드로 변경
    }

    private fun handleJung(key: String) {
        // 중성 입력 처리 로직
        currentMode = InputMode.JONG  // 종성 모드로 변경
    }

    private fun handleJong(key: String) {
        // 종성 입력 처리 로직
        currentMode = InputMode.CHO  // 다음 글자 입력을 위해 다시 초성 모드
    }

    private fun setKeyboardMode(key: String) {
        layoutMode = key
        val keyboardView = when (key) {
            "Korean" -> layoutInflater.inflate(R.layout.keyboard_korean_sebeol, null)
            "English" -> layoutInflater.inflate(R.layout.keyboard_english_qwerty, null)
            "Special" -> layoutInflater.inflate(R.layout.keyboard_special_1, null)
            else -> layoutInflater.inflate(R.layout.keyboard_korean_sebeol, null) // 기본값
        }
        setInputView(keyboardView)
        setupButtonListeners(keyboardView) // 버튼 리스너 설정
    }


    // 클릭 이벤트 핸들링
    private fun handleClick(v: View) {
        when (v.id) {
            R.id.key_english -> setKeyboardMode("English") // 한/영 키
            R.id.key_special -> setKeyboardMode("Special") // 특수문자 키
            else -> {
                val keyText = (v as Button).text.toString()
                handleKeyPress(keyText)
            }
        }
    }
    private fun handleKeyPress(keyText: String) {
        val ic = currentInputConnection ?: return

        when (keyText) {
            "⌫" -> ic.deleteSurroundingText(1, 0)  // 백스페이스
            "␣" -> ic.commitText(" ", 1)  // 스페이스
            "⏎" -> ic.commitText("\n", 1)  // 엔터
            else -> ic.commitText(keyText, 1)  // 일반 문자 입력
        }
    }
}

class HangulAutomata {
    private var cho: Char? = null  // 초성
    private var jung: Char? = null // 중성
    private var jong: Char? = null // 종성

    fun inputChar(c: Char): String {
        if (cho == null) { // 초성이 입력되지 않은 상태
            if (isCho(c)) {
                cho = c
                return cho.toString()  // 초성만 입력
            }
        } else if (jung == null) { // 초성이 있고 중성이 없는 상태
            if (isJung(c)) {
                jung = c
                return combineHangul(cho!!, jung!!)  // 초성과 중성 결합
            }
        } else if (jong == null) { // 초성과 중성이 있고 종성이 없는 상태
            if (isJong(c)) {
                jong = c
                return combineHangul(cho!!, jung!!, jong!!)  // 종성까지 결합
            }
        }
        return c.toString() // 조합 불가능한 경우 그냥 입력
    }

    fun reset() {
        cho = null
        jung = null
        jong = null
    }

    private fun isCho(c: Char) = c in "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
    private fun isJung(c: Char) = c in "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
    private fun isJong(c: Char) = c in "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ"

    private fun combineHangul(cho: Char, jung: Char, jong: Char? = null): String {
        val choIndex = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ".indexOf(cho)
        val jungIndex = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ".indexOf(jung)
        val jongIndex = if (jong != null) "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ".indexOf(jong) + 1 else 0

        val unicode = 0xAC00 + (choIndex * 21 * 28) + (jungIndex * 28) + jongIndex
        return unicode.toChar().toString()
    }
}