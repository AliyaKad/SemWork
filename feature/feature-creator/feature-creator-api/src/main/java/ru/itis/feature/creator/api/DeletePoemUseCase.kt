package ru.itis.feature.creator.api

interface DeletePoemUseCase {
    suspend operator fun invoke(poemId: String)
}