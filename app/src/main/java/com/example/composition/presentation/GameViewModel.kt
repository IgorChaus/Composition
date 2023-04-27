package com.example.composition.presentation

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.composition.R
import com.example.composition.data.GameRepositiryImpl
import com.example.composition.domain.entity.GameResult
import com.example.composition.domain.entity.GameSettings
import com.example.composition.domain.entity.Level
import com.example.composition.domain.entity.Question
import com.example.composition.domain.usecases.GenerateQuestionUseCase
import com.example.composition.domain.usecases.GetGameSettingsUseCase

class GameViewModel(application: Application): AndroidViewModel(application) {

    private val context = application

    private lateinit var gameSettings: GameSettings
    private lateinit var level: Level

    private val repository = GameRepositiryImpl
    private val generateQuestionUseCase = GenerateQuestionUseCase(repository)
    private val getGameSettingsUseCase = GetGameSettingsUseCase(repository)

    private var timer: CountDownTimer? = null

    private var countOfRightAnswers = 0
    private var countOfQuestions = 0

    private val _persentOfRightAnswer = MutableLiveData<Int>()
    val ersentOfRightAnswer: LiveData<Int>
        get() = _persentOfRightAnswer

    private val _progressAnswers = MutableLiveData<String>()
    val progressAnswers: LiveData<String>
        get() = _progressAnswers

    private val _formattedTime = MutableLiveData<String>()
    val formattedTime: LiveData<String>
        get() = _formattedTime

    private val _question = MutableLiveData<Question>()
    val question: LiveData<Question>
        get() = _question

    private val _enoughCount = MutableLiveData<Boolean>()
    val enoughCount: LiveData<Boolean>
        get() = _enoughCount

    private val _enoughPercent = MutableLiveData<Boolean>()
    val enoughPercent: LiveData<Boolean>
        get() = _enoughPercent

    private val _minPercent = MutableLiveData<Int>()
    val minPercent: LiveData<Int>
        get() = _minPercent

    private val _gameResult = MutableLiveData<GameResult>()
    val gameResult: LiveData<GameResult>
        get() = _gameResult

    fun startGame(level: Level){
        getGameSettings(level)
        startTimer()
        generateQuestion()
    }

    private fun updateProgress(){
        val percent = calculatePercentofRightAnswers()
        _persentOfRightAnswer.value = percent
        _progressAnswers.value = String.format(
            context.resources.getString(R.string.progress_answers),
            countOfRightAnswers,
            gameSettings.minCountOfRightAnswer
        )
        _enoughCount.value = countOfRightAnswers >= gameSettings.minCountOfRightAnswer
        _enoughPercent.value = percent >= gameSettings.minPercentOfRightAnswer
    }

    private fun calculatePercentofRightAnswers(): Int{
        return ((countOfRightAnswers / countOfQuestions.toDouble()) * 100).toInt()
    }

    fun chooseAnswer(number: Int){
        checkAnswer(number)
        updateProgress()
        generateQuestion()
    }

    private fun checkAnswer(number: Int){
        val rightAnswer = question.value?.rightAnswer
        if(number == rightAnswer){
            countOfRightAnswers++
        }
        countOfQuestions++
    }

    private fun getGameSettings(level: Level){
        this.level = level
        this.gameSettings = getGameSettingsUseCase(level)
        _minPercent.value = gameSettings.minPercentOfRightAnswer
    }

    private fun startTimer(){
        timer = object: CountDownTimer(
            gameSettings.gameTimeInSeconds * MILLIS_IN_SECONDS,
            MILLIS_IN_SECONDS
        ){
            override fun onTick(millisUntilFinished: Long) {
                _formattedTime.value = formatTime(millisUntilFinished)
            }
            override fun onFinish() {
                finishGame()
            }
        }
        timer?.start()
    }

    private fun generateQuestion(){
        _question.value = generateQuestionUseCase(gameSettings.maxSumValue)

    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }

    private fun formatTime(millisUntilFinished: Long): String{
        val seconds = millisUntilFinished / MILLIS_IN_SECONDS
        val minutes = seconds / SECONDS_IN_MINUTES
        val leftSeconds = seconds - (seconds * SECONDS_IN_MINUTES)
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun finishGame(){
        _gameResult.value = GameResult(
            enoughCount.value == true && enoughPercent.value == true,
            countOfRightAnswers,
            countOfQuestions,
            gameSettings
        )

    }

    companion object{
        private const val MILLIS_IN_SECONDS = 1000L
        private const val SECONDS_IN_MINUTES = 60
    }
}