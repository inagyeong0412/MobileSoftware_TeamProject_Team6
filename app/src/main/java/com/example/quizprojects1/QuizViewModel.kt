package com.example.quizprojects1

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.quizprojects1.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuizViewModel : ViewModel() {

    var uiState by mutableStateOf(QuizUiState())
        private set

    // 전체 문제(여기에서 주제 3개 이상 정의)
    private val allQuestions: List<Question> = listOf(
        // 일반 상식
        Question(
            id = 1,
            topic = "현대 음악",
            text = "다음 중 EDM 장르에 해당하는 음악 스타일은?",
            options = listOf("발라드", "클래식", "일렉트로닉 댄스", "국악"),
            correctIndex = 2
        ),
        Question(
            id = 2,
            topic = "현대 음악",
            text = "다음 중 현대 대중음악의 장르가 아닌 것은?",
            options = listOf("힙합", "R&B", "재즈", "판소리"),
            correctIndex = 3
        ),

        // 과학
        Question(
            id = 3,
            topic = "여행",
            text = "비행기에서 출발 전 수속을 밟는 과정은?",
            options = listOf("환승", "체크인", "보딩", "출국"),
            correctIndex = 1
        ),
        Question(
            id = 4,
            topic = "여행",
            text = "다음 중 국내 여행지가 아닌 곳은?",
            options = listOf("제주도", "경주", "오사카", "강릉"),
            correctIndex = 2
        ),

        // 영화
        Question(
            id = 5,
            topic = "문화",
            text = "문화의 특징으로 가장 알맞은 것은?",
            options = listOf("개인만 공유", "고정되어 변하지 않음", "세대 간 전승", "자연적으로 생성"),
            correctIndex = 2
        ),
        Question(
            id = 6,
            topic = "문화",
            text = "다음 중 유네스코 세계문화유산인 것은?",
            options = listOf("남산타워", "경복궁", "창덕궁", "롯데타워"),
            correctIndex = 2
        )
    )

    init {
        val topics = allQuestions.map { it.topic }.distinct()
        uiState = uiState.copy(
            topics = topics
        )
    }

    fun navigateToMain() {
        uiState = uiState.copy(
            currentScreen = Screen.MAIN,
            selectedTopic = null,
            questions = emptyList(),
            currentQuestionIndex = 0,
            currentScore = 0,
            currentGameAnswers = emptyList()
        )
    }

    fun navigateToWrongNote() {
        uiState = uiState.copy(currentScreen = Screen.WRONG_NOTE)
    }

    fun navigateToRanking() {
        uiState = uiState.copy(currentScreen = Screen.RANKING)
    }

    fun startQuiz(topic: String) {
        val qs = allQuestions.filter { it.topic == topic }
        uiState = uiState.copy(
            currentScreen = Screen.QUIZ,
            selectedTopic = topic,
            questions = qs,
            currentQuestionIndex = 0,
            currentScore = 0,
            currentGameAnswers = emptyList()
        )
    }

    /**
     * 현재 문제에 대한 답 제출
     */
    fun submitAnswer(selectedIndex: Int) {
        val questions = uiState.questions
        val index = uiState.currentQuestionIndex

        if (index !in questions.indices) return

        val question = questions[index]
        val isCorrect = (selectedIndex == question.correctIndex)

        val newScore = if (isCorrect) uiState.currentScore + 1 else uiState.currentScore

        // 현재 게임 답안 목록 업데이트
        val updatedAnswers = uiState.currentGameAnswers + UserAnswer(
            question = question,
            selectedIndex = selectedIndex,
            isCorrect = isCorrect
        )

        // 오답노트에 추가 (이미 있으면 중복 추가 안 함)
        val newWrongNote = if (!isCorrect) {
            if (uiState.wrongNote.any { it.id == question.id }) {
                uiState.wrongNote
            } else {
                uiState.wrongNote + question
            }
        } else {
            uiState.wrongNote
        }

        val nextIndex = index + 1
        if (nextIndex >= questions.size) {
            // 퀴즈 종료 → 결과 화면 이동 + 랭킹 저장
            val finalScore = newScore
            uiState = uiState.copy(
                currentScreen = Screen.RESULT,
                currentQuestionIndex = 0,
                currentScore = finalScore,
                currentGameAnswers = updatedAnswers,
                wrongNote = newWrongNote
            )
            saveRanking(finalScore)
        } else {
            // 다음 문제로 이동
            uiState = uiState.copy(
                currentQuestionIndex = nextIndex,
                currentScore = newScore,
                currentGameAnswers = updatedAnswers,
                wrongNote = newWrongNote
            )
        }
    }

    private fun saveRanking(score: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())

        val newEntry = RankingEntry(
            score = score,
            dateTime = dateStr
        )

        val newList = (uiState.rankings + newEntry)
            .sortedByDescending { it.score }

        uiState = uiState.copy(rankings = newList)
    }
}
