package com.example.lifeadvices11.data.models

enum class ActivityLevel(
    val key: String,
    val title: String,
    val description: String,
    val examples: String,
    val multiplier: Double,
    val icon: String
) {
    SEDENTARY(
        key = "sedentary",
        title = "Сидячий",
        description = "Минимум или отсутствие физической активности",
        examples = "Офисная работа, почти нет спорта, передвижение на транспорте",
        multiplier = 1.2,
        icon = "🪑"
    ),

    LIGHT(
        key = "light",
        title = "Легкая активность",
        description = "Легкие нагрузки 1-3 дня в неделю",
        examples = "Прогулки пешком, легкая уборка, редкие тренировки",
        multiplier = 1.375,
        icon = "🚶"
    ),

    MODERATE(
        key = "moderate",
        title = "Умеренная активность",
        description = "Средние нагрузки 3-5 дней в неделю",
        examples = "Фитнес, бег, плавание, танцы, активная работа",
        multiplier = 1.55,
        icon = "🏃"
    ),

    ACTIVE(
        key = "active",
        title = "Высокая активность",
        description = "Интенсивные тренировки 6-7 дней в неделю",
        examples = "Ежедневные тренировки, спортсмены, физическая работа",
        multiplier = 1.725,
        icon = "💪"
    ),

    EXTREME(
        key = "extreme",
        title = "⚡ Экстремальная активность",
        description = "Очень интенсивные нагрузки и физическая работа",
        examples = "Профессиональный спорт, строители, грузчики",
        multiplier = 1.9,
        icon = "⚡"
    )
}