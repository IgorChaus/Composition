package com.example.composition.domain.usecases

import com.example.composition.domain.entity.GameSettings
import com.example.composition.domain.entity.Question
import com.example.composition.domain.repository.GameRepository

class GenerateQuestionUseCase(
    private val repository: GameRepository
) {
    //Так как у нас в этом классе только один метод (generateQuestionUseCase)
    //то мы можем переменную generateQuestionUseCase вызвать как метод:
    //generateQuestionUseCase() или вызвать явно generateQuestionUseCase.invoke()
    operator fun invoke(maxSumValue: Int): Question{
        return repository.generateQuestion(maxSumValue, COUNT_OF_OPTIONS)

    }

    private companion object {

        private const val COUNT_OF_OPTIONS = 6

    }
}