package ru.itis.feature.creator.api


import ru.itis.core.models.Poem

interface SavePoemUseCase {
    suspend operator fun invoke(poem: Poem)
}
