Функциональные модули

1. Случайное стихотворение (feature-random)

2. Поиск по автору или названию (feature-search)
   (например, "Amy Levy" или "The Lost Friend")

3. Избранное (feature-favorites)

4. Авторизация (feature-auth)

5. Создание стихотворений (feature-creator)

Данные и API
Приложение использует публичное API PoetryDB (https://poetrydb.org)

Юнит-тесты
Написаны во всех фича модулях на примерно 50% юзкейсов:

feature-auth:
LoginUseCaseImplTest.kt
RegisterUseCaseImplTest.kt

feature-creator:
DeletePoemUseCaseImplTest.kt
SavePoemUseCaseImplTest.kt

feature-favorites:
AddToFavoritesUseCaseImplTest.kt
RemoveFromFavoritesUseCaseImplTest.kt
ToggleFavoriteUseCaseImplTest.kt

feature-random:
GetRandomPoemUseCaseImplTest.kt

feature-search:
SearchPoemsUseCaseImplTest.kt
