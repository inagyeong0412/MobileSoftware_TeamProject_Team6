package com.example.quizprojects1.model

enum class Screen {
    MAIN,
    QUIZ,
    RESULT,
    WRONG_NOTE,
    RANKING
}

data class Question(
    val id: Int,
    val topic: String,        // 주제 (예: "일반 상식", "과학", "영화")
    val text: String,         // 문제
    val options: List<String>,// 4지선다 보기
    val correctIndex: Int     // 정답 인덱스 (0~3)
)

data class UserAnswer(
    val question: Question,
    val selectedIndex: Int,
    val isCorrect: Boolean
)

data class RankingEntry(
    val score: Int,
    val dateTime: String      // "2025-12-11 09:30" 이런 식 문자열
)

data class QuizUiState(
    val currentScreen: Screen = Screen.MAIN,
    val topics: List<String> = emptyList(),
    val selectedTopic: String? = null,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val currentScore: Int = 0,
    val currentGameAnswers: List<UserAnswer> = emptyList(),
    val wrongNote: List<Question> = emptyList(),
    val rankings: List<RankingEntry> = emptyList()
)
