package com.example.quizprojects1

import android.media.MediaPlayer
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quizprojects1.ui.theme.QuizAppTheme
import com.example.quizprojects1.model.Screen
import com.example.quizprojects1.model.QuizUiState

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

class MainActivity : ComponentActivity() {

    private val quizViewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuizAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuizApp(
                        viewModel = quizViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun QuizApp(viewModel: QuizViewModel) {
    val uiState = viewModel.uiState

    when (uiState.currentScreen) {
        Screen.MAIN -> MainScreen(
            uiState = uiState,
            onTopicClick = { topic -> viewModel.startQuiz(topic) },
            onWrongNoteClick = { viewModel.navigateToWrongNote() },
            onRankingClick = { viewModel.navigateToRanking() }
        )

        Screen.QUIZ -> QuizScreen(
            uiState = uiState,
            onSubmitAnswer = { selectedIndex ->
                viewModel.submitAnswer(selectedIndex)
            },
            onBackToMain = { viewModel.navigateToMain() }
        )

        Screen.RESULT -> ResultScreen(
            uiState = uiState,
            onGoToMain = { viewModel.navigateToMain() },
            onGoToWrongNote = { viewModel.navigateToWrongNote() },
            onGoToRanking = { viewModel.navigateToRanking() }
        )

        Screen.WRONG_NOTE -> WrongNoteScreen(
            uiState = uiState,
            onBackToMain = { viewModel.navigateToMain() }
        )

        Screen.RANKING -> RankingScreen(
            uiState = uiState,
            onBackToMain = { viewModel.navigateToMain() }
        )
    }
}

/* ---------- 사운드 재생 함수 (정답/오답 효과음) ---------- */

fun playCorrectSound(context: Context) {
    val mp = MediaPlayer.create(context, R.raw.correct)
    mp.setOnCompletionListener { it.release() }
    mp.start()
}

fun playWrongSound(context: Context) {
    val mp = MediaPlayer.create(context, R.raw.wrong)
    mp.setOnCompletionListener { it.release() }
    mp.start()
}

/* ---------- 메인 화면 (주제 선택 + 오답노트/랭킹 버튼) ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: QuizUiState,
    onTopicClick: (String) -> Unit,
    onWrongNoteClick: () -> Unit,
    onRankingClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        color = Color(0xFFEDE9FE),   // 연보라 배경
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = "퀴즈 앱",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1) 배경 이미지
            Image(
                painter = painterResource(id = R.drawable.sky_bg10),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 2) 가독성 오버레이 (숫자 올리면 더 뿌옇게 됨)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.70f))
            )

            // 3) 기존 UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 헤더 배너
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.90f)
                    ),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "퀴즈 주제를 선택하세요",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "오답노트로 복습하고 랭킹 1등에 도전!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 주제 그리드
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.topics) { topic ->
                        TopicCard(
                            topic = topic,
                            onClick = { onTopicClick(topic) }
                        )
                    }
                }

                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onWrongNoteClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.EditNote, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("오답 노트")
                    }

                    FilledTonalButton(
                        onClick = onRankingClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("랭킹")
                    }
                }
            }
        }
    }
}


@Composable
private fun TopicCard(
    topic: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "scale")

    val (icon, subText) = topicVisual(topic)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Column {
                Text(topic, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun topicVisual(topic: String): Pair<ImageVector, String> =
    when (topic) {
        "일반 상식" -> Icons.Filled.Public to "상식 퀴즈로 두뇌 워밍업!"
        "과학" -> Icons.Filled.Science to "재미있는 과학 상식!"
        "영화" -> Icons.Filled.Movie to "영화 덕후 도전!"
        else -> Icons.Filled.Public to "퀴즈를 시작해요!"
    }


/* ---------- 퀴즈 화면 ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    uiState: QuizUiState,
    onSubmitAnswer: (Int) -> Unit,
    onBackToMain: () -> Unit
) {
    val context = LocalContext.current
    val questions = uiState.questions
    val currentIndex = uiState.currentQuestionIndex

    if (questions.isEmpty() || currentIndex !in questions.indices) {
        onBackToMain()
        return
    }

    val currentQuestion = questions[currentIndex]
    var selectedIndex by remember(currentQuestion.id) { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        color = Color(0xFFEDE9FE),   // 연보라 배경
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = "${uiState.selectedTopic ?: ""} 퀴즈",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            /* 1️⃣ 배경 이미지 */
            Image(
                painter = painterResource(id = R.drawable.sky_bg14),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            /* 2️⃣ 반투명 오버레이 (글자 안 묻히게) */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.75f))
            )

            /* 3️⃣ 실제 UI */
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /* 문제 카드 */
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEDE9FE)
                    ),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "문제 ${currentIndex + 1} / ${questions.size}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQuestion.text,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                /* 보기 선택 */
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSelected = selectedIndex == index

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable { selectedIndex = index },
                            shape = MaterialTheme.shapes.large,
                            color = if (isSelected)
                                Color(0xFFE9D5FF)   // 선택됨
                            else
                                Color(0xFFEFF6FF),  // 기본
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF7C3AED) else Color(0xFFCBD5E1)
                            ),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected)
                                        Color(0xFF6D28D9)
                                    else
                                        Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }

                /* 정답 제출 버튼 */
                Button(
                    onClick = {
                        val answerIndex = selectedIndex ?: return@Button
                        val isCorrect = (answerIndex == currentQuestion.correctIndex)

                        if (isCorrect) playCorrectSound(context)
                        else playWrongSound(context)

                        onSubmitAnswer(answerIndex)
                        selectedIndex = null
                    },
                    enabled = selectedIndex != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedIndex != null)
                            Color(0xFF7C3AED)
                        else
                            Color(0xFFF1EAFE),
                        contentColor = if (selectedIndex != null)
                            Color.White
                        else
                            Color(0xFF64748B)
                    )
                ) {
                    Text(
                        text = "정답 제출",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}


/* ---------- 결과 화면 ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    uiState: QuizUiState,
    onGoToMain: () -> Unit,
    onGoToWrongNote: () -> Unit,
    onGoToRanking: () -> Unit
) {
    val total = uiState.currentGameAnswers.size
    val score = uiState.currentScore

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        color = Color(0xFFEDE9FE),   // 연보라 배경
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = "결과 화면",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1) 배경 이미지
            Image(
                painter = painterResource(id = R.drawable.sky_bg4),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 2) 가독성 오버레이
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.75f))
            )

            // 3) UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 점수 카드(강조)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE9FE)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "최종 점수",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "$score / $total",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "정답 / 오답 확인",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = true)
                ) {
                    itemsIndexed(uiState.currentGameAnswers) { idx, ans ->
                        val resultText = if (ans.isCorrect) "정답" else "오답"

                        val my = ans.question.options.getOrNull(ans.selectedIndex) ?: "(선택 없음)"
                        val correct = ans.question.options.getOrNull(ans.question.correctIndex) ?: "(정답 정보 없음)"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(
                                containerColor = if (ans.isCorrect)
                                    Color(0xFFE8F5E9)   // 정답 연초록
                                else
                                    Color(0xFFFFEBEE)   // 오답 연핑크
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Q${idx + 1}. ${ans.question.text}", fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text("내 답: $my")
                                Text("정답: $correct")
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = resultText,
                                    color = if (ans.isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onGoToMain,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) { Text("메인") }

                    FilledTonalButton(
                        onClick = onGoToWrongNote,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) { Text("오답 노트") }

                    FilledTonalButton(
                        onClick = onGoToRanking,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) { Text("랭킹") }
                }
            }
        }
    }
}


/* ---------- 오답 노트 화면 ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrongNoteScreen(
    uiState: QuizUiState,
    onBackToMain: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        color = Color(0xFFEDE9FE),   // 연보라 배경
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = "오답 노트",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 배경 이미지
            Image(
                painter = painterResource(id = R.drawable.sky_bg10),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 오버레이
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.75f))
            )

            // UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                if (uiState.wrongNote.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "저장된 오답이 없습니다.",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(uiState.wrongNote) { index, q ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFEBEE) // 연핑크
                                ),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Q${index + 1} (${q.topic})",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(q.text, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("정답: ${q.options[q.correctIndex]}")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onBackToMain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("메인 화면으로")
                }
            }
        }
    }
}


/* ---------- 랭킹 화면 ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    uiState: QuizUiState,
    onBackToMain: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        color = Color(0xFFEDE9FE),   // 연보라 배경
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = "내 랭킹",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 배경 이미지
            Image(
                painter = painterResource(id = R.drawable.sky_bg8),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 오버레이
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.75f))
            )

            // UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                if (uiState.rankings.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아직 기록된 점수가 없습니다.",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(uiState.rankings) { index, entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFEDE9FE) // 연보라
                                ),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "${index + 1}위",
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text("점수: ${entry.score}")
                                    }
                                    Text(entry.dateTime)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onBackToMain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("메인 화면으로")
                }
            }
        }
    }
}

