package com.francislab.kyboard

import android.annotation.SuppressLint
import android.inputmethodservice.InputMethodService
import android.view.inputmethod.ExtractedTextRequest
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import kotlin.math.abs
import android.util.Log

class KyboardService : InputMethodService() {
//    private val hangulAutomata: HangulAutomata = HangulAutomata()
    private var currentMode = 0 // 0 == 초성, 1 == 중성, 2 == 종성
    private var composingText: String = "" // 조합 중인 텍스트를 저장할 변수
    private var startX: Float = 0f
    private var startY: Float = 0f

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_korean_sebeol, null)
        setupButtonListeners(keyboardView)
        return keyboardView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupButtonListeners(keyboardView: View) {
        val buttons = R.id::class.java.fields.map { it.getInt(null) }

        buttons.forEach { id ->
            keyboardView.findViewById<Button>(id)?.setOnTouchListener { v, event -> handleTouch(v, event) }
        }
    }

    // 터치 이벤트 핸들링
    private fun handleTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                val deltaX = event.x - startX
                val deltaY = event.y - startY
                when (currentMode) {
                    0 -> handleCho(v.id)  // 초성 입력 처리
                    1 -> handleJung(v.id)  // 중성 입력 처리
                    2 -> handleJong(v.id)  // 종성 입력 처리
                }
                if (abs(deltaX) < 5 && abs(deltaY) < 5) handleKeyPress(v.id)
                else handleSwipe(v.id, deltaX, deltaY)
//                val combinedText = hangulAutomata.inputChar(keyId)
//                currentInputConnection.setComposingText(combinedText, 1)
                return true
            }
        }
        return false
    }

    private fun handleKeyPress(keyId: Int): String {
        val ic = currentInputConnection
        val newText = when (currentMode) {
            0 -> when (keyId) {
                R.id.key_01 -> "ㅇ"
                R.id.key_02 -> "ㄱ"
                R.id.key_04 -> "ㄴ"
                R.id.key_05 -> "ㄷ"
                R.id.key_07 -> "ㅁ"
                R.id.key_08 -> "ㅂ"
                R.id.key_09 -> "ㅅ"
                R.id.key_10 -> "ㅈ"
                else -> ""
            }
            1 -> when (keyId) {
                R.id.key_02 -> "ㅗ"
                R.id.key_03 -> "ㅏ"
                R.id.key_05 -> "ㅜ"
                R.id.key_06 -> "ㅓ"
                R.id.key_08 -> "ㅐ"
                R.id.key_09 -> "ㅣ"
                R.id.key_11 -> "ㅔ"
                R.id.key_12 -> "ㅡ"
                else -> ""
            }
            2 -> when (keyId) {
                R.id.key_03 -> "ㅇ"
                R.id.key_06 -> "ㄴ"
                R.id.key_09 -> "ㄹ"
                R.id.key_12 -> "ㅅ"
                else -> ""
            }
            else -> ""
        }

        when (keyId) {
            R.id.key_backspace -> handleBackspace()  // 백스페이스
            R.id.key_korean -> setKeyboardMode(keyId)
            R.id.key_english -> setKeyboardMode(keyId) // 한/영 키
            R.id.key_space -> handleSpace()  // 스페이스
            R.id.key_enter -> ic.commitText("\n", 1)  // 엔터
            R.id.key_exclamation -> ic.commitText("!", 1)  // !
            R.id.key_question -> ic.commitText("?", 1)  // ?
            R.id.key_comma -> ic.commitText(",", 1)  // ,
            R.id.key_dot -> ic.commitText(".", 1)  // .
            R.id.key_semicolon -> ic.commitText(";", 1)  // ;
            R.id.key_paren_open -> ic.commitText("(", 1)  // (
            R.id.key_paren_close -> ic.commitText(")", 1)  // )
        }
        composingText = newText
        ic.setComposingText(newText, 1)
        return newText
    }

    private fun handleSwipe(keyId: Int, deltaX: Float, deltaY: Float) {
        if (abs(deltaX) > abs(deltaY)) {
            // 좌/우 드래그
            if (deltaX > 5) handleSwipeRight(keyId)
            else if (deltaX < -5) handleSwipeLeft(keyId)
        } else {
            // 상/하 드래그
            if (deltaY > 5) handleSwipeDown(keyId)
            else if (deltaY < -5) handleSwipeUp(keyId)
        }
    }

    private fun handleSwipeLeft(keyId: Int) {
        val ic = currentInputConnection ?: return
        val newText = when (keyId) {
            R.id.key_01 -> "ㅇ"
            R.id.key_02 -> "ㄱ"
            R.id.key_03 -> ""
            R.id.key_04 -> "ㄴ"
            R.id.key_05 -> "ㄷ"
            R.id.key_06 -> ""
            R.id.key_07 -> "ㅁ"
            R.id.key_08 -> "ㅂ"
            R.id.key_09 -> "ㅅ"
            R.id.key_10 -> "ㅈ"
            else -> ""
        }
        ic.commitText(newText, 1)
    }

    private fun handleSwipeRight(keyId: Int) {
        val ic = currentInputConnection ?: return
        val newText = when (keyId) {
            R.id.key_01-> "1"
            R.id.key_02-> "2"
            R.id.key_03-> "3"
            R.id.key_04-> "4"
            R.id.key_05-> "5"
            R.id.key_06-> "6"
            R.id.key_07-> "7"
            R.id.key_08-> "8"
            R.id.key_09-> "9"
            R.id.key_10-> "@"
            R.id.key_11-> "0"
            R.id.key_12-> "."
            else -> ""
        }
        ic.commitText(newText, 1)
    }

    private fun handleSwipeUp(keyId: Int) {
        val ic = currentInputConnection ?: return
        val newText = when (keyId) {
            R.id.key_01-> "1"
            R.id.key_02-> "2"
            R.id.key_03-> "3"
            R.id.key_04-> "4"
            R.id.key_05-> "5"
            R.id.key_06-> "6"
            R.id.key_07-> "7"
            R.id.key_08-> "8"
            R.id.key_09-> "9"
            R.id.key_10-> "@"
            R.id.key_11-> "0"
            R.id.key_12-> "."
            else -> ""
        }
        ic.commitText(newText, 1)
    }

    private fun handleSwipeDown(keyId: Int) {
        val ic = currentInputConnection ?: return
        val newText = when (currentMode) {
            0 -> when (keyId) {
                R.id.key_01 -> "ㅎ"
                R.id.key_02 -> "ㅋ"
                R.id.key_04 -> "ㅁ"
                R.id.key_05 -> "ㅌ"
                R.id.key_08 -> "ㅍ"
                R.id.key_11 -> "ㅊ"
                else -> ""
            }

            1 -> when (keyId) {
                R.id.key_01 -> "ㅎ"
                R.id.key_02 -> "ㅋ"
                R.id.key_04 -> "ㅁ"
                R.id.key_05 -> "ㅌ"
                R.id.key_08 -> "ㅍ"
                R.id.key_11 -> "ㅊ"
                else -> ""
            }

            2 -> when (keyId) {
                R.id.key_01 -> "ㅎ"
                R.id.key_02 -> "ㅋ"
                R.id.key_04 -> "ㅁ"
                R.id.key_05 -> "ㅌ"
                R.id.key_08 -> "ㅍ"
                R.id.key_11 -> "ㅊ"
                else -> ""
            }
            else -> ""
        }
        currentMode = when (currentMode) {
            1 -> when (keyId) {
                R.id.key_01 -> 0
                else -> (currentMode + 1) % 3
            }
            else -> (currentMode + 1) % 3
        }
        ic.commitText(newText, 1)
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return

        // 조합 중인 텍스트 가져오기
        val composingText = ic.getComposingText(Int.MAX_VALUE)

        if (composingText != null && composingText.isNotEmpty()) {
            // 커서 고정 상태: 조합 중인 텍스트에서 한 글자씩 삭제
            val newText = composingText.dropLast(1)
            if (newText.isNotEmpty()) {
                ic.setComposingText(newText, 1) // 조합 텍스트 업데이트
            } else {
                ic.finishComposingText() // 조합 텍스트 종료
            }
        } else {
            // 커서 고정 상태가 아님: 커서 앞의 전체 문자 삭제
            val beforeCursor = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)?.toString() ?: ""
            if (beforeCursor.isNotEmpty()) {
                ic.deleteSurroundingText(beforeCursor.length, 0)
            }
        }
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return

        // 조합 중인 텍스트 가져오기
        val composingText = ic.getComposingText(Int.MAX_VALUE)

        if (composingText != null && composingText.isNotEmpty()) {
            // 커서 고정 상태: 조합 중인 텍스트를 완성하고 커서 고정 상태 해제
            ic.finishComposingText()
        } else {
            // 커서 고정 상태가 아님: 스페이스 문자 추가
            ic.commitText(" ", 1)
        }
    }


    private fun handleCho(keyId: Int) {
        // 초성 입력 처리 로직
        currentMode = 1  // 중성 모드로 변경
    }

    private fun handleJung(keyId: Int) {
        // 중성 입력 처리 로직
        currentMode = 2  // 종성 모드로 변경
    }

    private fun handleJong(keyId: Int) {
        // 종성 입력 처리 로직
        currentMode = 0  // 다음 글자 입력을 위해 다시 초성 모드
    }

    private fun setKeyboardMode(keyId: Int) {
        val keyboardView = when (keyId) {
            R.id.key_korean -> layoutInflater.inflate(R.layout.keyboard_korean_sebeol, null)
            R.id.key_english -> layoutInflater.inflate(R.layout.keyboard_english_qwerty, null)
            R.id.key_special_1 -> layoutInflater.inflate(R.layout.keyboard_special_1, null)
            R.id.key_special_2 -> layoutInflater.inflate(R.layout.keyboard_special_2, null)
            R.id.key_special_3 -> layoutInflater.inflate(R.layout.keyboard_special_3, null)
            else -> layoutInflater.inflate(R.layout.keyboard_korean_sebeol, null) // 기본값
        }
        setInputView(keyboardView)
        setupButtonListeners(keyboardView) // 버튼 리스너 설정
    }

}


//class HangulAutomata {
//    private var cho: KeyId? = null  // 초성
//    private var jung: KeyId? = null // 중성
//    private var jong: KeyId? = null // 종성
//
//    fun inputChar(c: KeyId): String {
//        if (cho == null) { // 초성이 입력 되지 않은 상태
//            if (isCho(c)) {
//                cho = c
//                return cho.toString()  // 초성만 입력
//            }
//        } else if (jung == null) { // 초성이 있고 중성이 없는 상태
//            if (isJung(c)) {
//                jung = c
//                return combineHangul(cho!!, jung!!)  // 초성과 중성 결합
//            }
//        } else if (jong == null) { // 초성과 중성이 있고 종성이 없는 상태
//            if (isJong(c)) {
//                jong = c
//                return combineHangul(cho!!, jung!!, jong!!)  // 종성까지 결합
//            }
//        }
//        return c.toString() // 조합 불가능한 경우 그냥 입력
//    }
//
//    fun reset() {
//        cho = null
//        jung = null
//        jong = null
//    }
//
//    private fun isCho(c: Char) = c in "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
//    private fun isJung(c: Char) = c in "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
//    private fun isJong(c: Char) = c in "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ"
//
//    private fun combineHangul(cho: Char, jung: Char, jong: Char? = null): String {
//        val choIndex = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ".indexOf(cho)
//        val jungIndex = "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ".indexOf(jung)
//        val jongIndex = if (jong != null) "ㄱㄲㄳㄴㄵㄶㄷㄹㄺㄻㄼㄽㄾㄿㅀㅁㅂㅄㅅㅆㅇㅈㅊㅋㅌㅍㅎ".indexOf(jong) + 1 else 0
//
//        val letter = (choIndex * 21 + jungIndex) *  28 + jongIndex + 0xAC00
//        return letter.toChar().toString()
//    }
//}