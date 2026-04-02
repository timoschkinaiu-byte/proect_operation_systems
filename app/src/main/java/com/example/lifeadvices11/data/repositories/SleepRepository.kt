package com.example.lifeadvices11.data.repositories

import com.example.lifeadvices11.data.dao.SleepDao
import com.example.lifeadvices11.data.entities.SleepProfileEntity
import com.example.lifeadvices11.data.entities.DailySleepEntity
import com.example.lifeadvices11.data.entities.SleepPracticeEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

class SleepRepository(
    private val sleepDao: SleepDao
) {
    // Profile operations
    fun getSleepProfile(): Flow<SleepProfileEntity?> = sleepDao.getSleepProfile()

    suspend fun getSleepProfileSync(): SleepProfileEntity? = sleepDao.getSleepProfileSync()

    suspend fun createSleepProfileIfNotExists() {
        val existingProfile = sleepDao.getSleepProfileSync()
        if (existingProfile == null) {
            sleepDao.insertSleepProfile(SleepProfileEntity(id = 1))
        }
    }

    suspend fun saveSleepOnboardingData(
        targetHours: Double,
        bedTime: String,
        wakeTime: String,
        quality: String,
        issues: String,
        preferredWakeTime: String
    ) {
        sleepDao.saveSleepOnboardingData(targetHours, bedTime, wakeTime, quality, issues, preferredWakeTime)
    }

    suspend fun hasCompletedSleepOnboarding(): Boolean {
        return sleepDao.hasCompletedSleepOnboarding() ?: false
    }

    // Daily tracking operations
    suspend fun addSleepEntry(entry: DailySleepEntity) {
        sleepDao.insertDailySleep(entry)
    }

    suspend fun getTodaySleep(): DailySleepEntity? {
        val todayStart = getStartOfDay()
        return sleepDao.getDailySleepByDate(todayStart)
    }

    suspend fun getLastWeekSleep(): List<DailySleepEntity> {
        return sleepDao.getLastWeekSleep()
    }

    // Practices
    suspend fun getAllPractices(): List<SleepPracticeEntity> {
        return sleepDao.getAllPractices()
    }

    suspend fun updatePractice(practice: SleepPracticeEntity) {
        sleepDao.updatePractice(practice)
    }

    suspend fun initializePracticesIfNeeded() {
        val practices = sleepDao.getAllPractices()
        if (practices.isEmpty()) {
            val profile = getSleepProfileSync()
            if (profile != null && profile.hasCompletedSleepOnboarding) {
                val personalizedPractices = generatePersonalizedPractices(profile)
                sleepDao.insertPractices(personalizedPractices)
            }
        }
    }

    suspend fun generatePersonalizedPractices(profile: SleepProfileEntity): List<SleepPracticeEntity> {
        val practices = mutableListOf<SleepPracticeEntity>()

        practices.addAll(getBasePractices())

        when (profile.sleepIssues) {
            "insomnia" -> practices.addAll(getInsomniaPractices())
            "snoring" -> practices.addAll(getSnoringPractices())
            "night_wakeups" -> practices.addAll(getNightWakeupsPractices())
            "none" -> practices.addAll(getMaintenancePractices())
        }

        when (profile.sleepQuality) {
            "poor" -> practices.addAll(getPoorQualityPractices())
            "normal" -> practices.addAll(getNormalQualityPractices())
            "good" -> practices.addAll(getGoodQualityPractices())
        }

        if (profile.targetSleepHours < 7) {
            practices.addAll(getLowSleepHoursPractices())
        } else if (profile.targetSleepHours > 9) {
            practices.addAll(getHighSleepHoursPractices())
        }

        return practices
    }

    private fun getBasePractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Дыхательная техника 4-7-8",
                shortDescription = "Успокаивает нервную систему за 3 минуты",
                fullDescription = "Эта техника дыхания была разработана доктором Эндрю Вейлом. Она основана на древней пранаяме и помогает быстро успокоиться, снизить тревожность и подготовиться ко сну. Методика работает за счёт удлинения выдоха, что активирует парасимпатическую нервную систему и замедляет сердечный ритм.",
                steps = "1. Сядьте прямо или лягте на спину. Кончик языка должен касаться нёба за верхними зубами.\n2. Полностью выдохните через рот, издавая лёгкий свистящий звук.\n3. Закройте рот и медленно вдохните через нос на 4 секунды.\n4. Задержите дыхание на 7 секунд.\n5. Выдохните через рот на 8 секунд, снова издавая свистящий звук.\n6. Повторите цикл 4-8 раз.",
                duration = 3,
                category = "breathing",
                benefits = "Снижает тревожность и стресс\nЗамедляет сердечный ритм\nСнижает давление\nПомогает уснуть за 5-10 минут",
                contraindications = "Не рекомендуется при низком давлении\nПри обострении респираторных заболеваний"
            ),
            SleepPracticeEntity(
                title = "Гигиена сна",
                shortDescription = "Комплекс привычек для качественного сна",
                fullDescription = "Гигиена сна - это набор поведенческих и экологических практик, которые подготавливают организм ко сну. Многие проблемы со сном вызваны не медицинскими причинами, а нарушением ритуалов и условий.",
                steps = "1. Определите постоянное время отхода ко сну и пробуждения\n2. За 1-2 часа до сна прекратите использовать электронные устройства\n3. Сделайте освещение в комнате приглушённым\n4. Примите тёплую ванну или душ за 60-90 минут до сна\n5. Проветрите спальню\n6. Не ешьте плотно за 2-3 часа до сна\n7. Исключите кофеин после 14 часов\n8. Используйте кровать только для сна",
                duration = 0,
                category = "habit",
                benefits = "Улучшает качество сна\nПомогает быстрее засыпать\nУменьшает ночные пробуждения\nПовышает дневную энергию",
                contraindications = "При сменной работе необходима адаптация"
            )
        )
    }

    private fun getInsomniaPractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Прогрессивная мышечная релаксация",
                shortDescription = "Снимает физическое напряжение при бессоннице",
                fullDescription = "Метод прогрессивной релаксации был разработан американским врачом Эдмундом Джекобсоном. Он основан на принципе: после напряжения мышца автоматически расслабляется глубже. Последовательно напрягая и расслабляя различные группы мышц, вы учитесь распознавать и контролировать мышечное напряжение.",
                steps = "1. Лягте в удобное положение, закройте глаза\n2. Начните с правой стопы: сильно напрягите пальцы и всю стопу на 5 секунд\n3. Резко расслабьте на 15 секунд, чувствуя тепло и тяжесть\n4. Левая стопа: повторите то же самое\n5. Икры и голени: напрягите, потянув носки на себя\n6. Бёдра и ягодицы: сожмите ягодицы и напрягите бёдра\n7. Живот: втяните и напрягите пресс\n8. Грудная клетка: сделайте глубокий вдох и задержите\n9. Кисти рук: сожмите в кулаки\n10. Предплечья и плечи: напрягите бицепсы и трицепсы\n11. Плечи: поднимите к ушам, затем резко опустите\n12. Шея: медленно поверните голову влево, затем вправо\n13. Лицо: наморщите лоб, зажмурьтесь, сожмите челюсти\n14. Завершение: полежите 2-3 минуты, ощущая всё тело расслабленным",
                duration = 15,
                category = "relaxation",
                benefits = "Снимает хроническое мышечное напряжение\nУменьшает головные боли\nСнижает уровень кортизола\nУлучшает качество сна\nПомогает при тревожных расстройствах",
                contraindications = "При острых травмах мышц или суставов\nПри недавних операциях"
            ),
            SleepPracticeEntity(
                title = "Ограничение сна",
                shortDescription = "Метод когнитивно-поведенческой терапии бессонницы",
                fullDescription = "Метод ограничения сна - один из ключевых компонентов когнитивно-поведенческой терапии бессонницы. Он заключается во временном сокращении времени, проводимого в постели, до фактического времени сна. Это помогает укрепить ассоциацию между кроватью и сном.",
                steps = "1. Ведите дневник сна в течение 1-2 недель\n2. Рассчитайте среднее фактическое время сна за ночь\n3. Установите время в постели равное среднему времени сна (минимум 5 часов)\n4. Установите фиксированное время пробуждения\n5. Ложитесь спать так, чтобы время в постели соответствовало расчёту\n6. Когда эффективность сна достигнет 85%, увеличьте время в постели на 15-20 минут\n7. Повторяйте, пока не достигнете целевой продолжительности сна",
                duration = 0,
                category = "therapy",
                benefits = "Повышает эффективность сна\nУкрепляет связь кровать-сон\nСокращает время засыпания\nУменьшает количество ночных пробуждений",
                contraindications = "Не рекомендуется при эпилепсии\nПри биполярном расстройстве\nПри некоторых формах апноэ сна"
            )
        )
    }

    private fun getSnoringPractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Упражнения от храпа",
                shortDescription = "Укрепление мышц горла и языка",
                fullDescription = "Храп часто возникает из-за слабости мышц мягкого нёба и языка. Регулярное выполнение специальных упражнений укрепляет эти мышцы и может значительно уменьшить или устранить храп.",
                steps = "1. Упражнение 'Язык вперёд': высуньте язык как можно дальше вперёд, удерживайте 5 секунд, повторите 10 раз\n2. Упражнение 'Язык вверх': прижмите язык к нёбу, скользите им назад к горлу, повторите 10 раз\n3. Упражнение 'Звук А': широко откройте рот, произнесите 'А-А-А' напряжённо, повторите 10 раз\n4. Упражнение 'Сопротивление': надавите пальцем на подбородок, открывайте рот с сопротивлением\n5. Упражнение 'Жевание': жуйте с закрытым ртом, сильно напрягая челюсти, 2 минуты\n6. Напрягайте мягкое нёбо, произнося звук 'Ы' с закрытым ртом\n7. Выполняйте упражнения 2-3 раза в день",
                duration = 10,
                category = "exercises",
                benefits = "Укрепляет мышцы горла\nУменьшает интенсивность храпа\nУлучшает качество сна партнёра\nМожет помочь при лёгком апноэ",
                contraindications = "При обострении тонзиллита\nПосле операций на горле"
            )
        )
    }

    private fun getNightWakeupsPractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Техника возвращения ко сну",
                shortDescription = "Что делать при ночном пробуждении",
                fullDescription = "Ночные пробуждения - нормальное явление. Проблема возникает, когда вы не можете заснуть снова. Эта техника поможет вам быстро вернуться в сон без нарастающей тревоги.",
                steps = "1. Не смотрите на часы! Это только усиливает тревогу\n2. Не вставайте с кровати, если не прошло более 20 минут\n3. Сделайте 5-10 циклов дыхания 4-7-8\n4. Сосредоточьтесь на ощущениях в теле, а не на мыслях\n5. Представьте спокойное место во всех деталях\n6. Если тревога сильная - включите тусклый свет и почитайте бумажную книгу\n7. Не берите телефон и не включайте телевизор\n8. Вернитесь в кровать когда почувствуете сонливость\n9. Не ругайте себя за пробуждение - это нормально\n10. Помните: просто лежать с закрытыми глазами уже даёт отдых",
                duration = 0,
                category = "technique",
                benefits = "Снижает тревогу при ночных пробуждениях\nУчит быстро возвращаться ко сну\nУменьшает время бодрствования ночью\nПовышает уверенность в своей способности спать",
                contraindications = "Не заменяет лечение при серьёзных нарушениях сна"
            )
        )
    }

    private fun getMaintenancePractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Поддержание здорового сна",
                shortDescription = "Как сохранить хороший сон в долгосрочной перспективе",
                fullDescription = "У вас нет проблем со сном, но профилактика поможет сохранить это здоровое состояние. Эти практики помогут укрепить вашу устойчивость к стрессу и предотвратить возможные нарушения сна в будущем.",
                steps = "1. Продолжайте придерживаться постоянного графика сна\n2. Регулярно практикуйте техники релаксации\n3. Обеспечьте физическую активность днём\n4. Следите за световым режимом: яркий свет днём, приглушённый вечером\n5. Раз в месяц проводите 'ревизию' гигиены сна\n6. Ведите дневник сна для отслеживания паттернов\n7. Практикуйте благодарность перед сном\n8. Создайте ритуал отхода ко сну, который вам нравится",
                duration = 0,
                category = "maintenance",
                benefits = "Предотвращает развитие нарушений сна\nУкрепляет психологическую устойчивость\nПовышает осознанность\nУлучшает общее качество жизни",
                contraindications = "Нет"
            )
        )
    }

    private fun getPoorQualityPractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Сканирование тела",
                shortDescription = "Медитация для глубокого расслабления",
                fullDescription = "Сканирование тела - одна из ключевых практик майндфулнесс. Она учит направлять внимание в разные части тела без оценки и желания что-то изменить. Это помогает отключиться от мыслей, снизить уровень кортизола и переключить нервную систему в режим отдыха.",
                steps = "1. Лягте на спину, руки вдоль тела, ноги на ширине плеч\n2. Закройте глаза и сделайте 5 глубоких вдохов\n3. Направьте внимание на левую стопу. Почувствуйте любые ощущения\n4. Поднимитесь выше: левая голень, левое колено, левое бедро\n5. Переместитесь на правую ногу: стопа, голень, колено, бедро\n6. Таз и область живота: почувствуйте, как живот поднимается с дыханием\n7. Поясница и нижняя часть спины\n8. Грудная клетка и верхняя часть спины\n9. Левая рука: кисть, предплечье, локоть, плечо\n10. Правая рука: кисть, предплечье, локоть, плечо\n11. Шея и горло: мягко, без напряжения\n12. Лицо: челюсти, губы, щёки, глаза, лоб\n13. Макушка головы\n14. Почувствуйте всё тело целиком как единое целое\n15. Если отвлеклись на мысли - мягко верните внимание",
                duration = 20,
                category = "meditation",
                benefits = "Снижает тревожность и депрессию\nУлучшает способность концентрироваться\nПомогает при хронической боли\nУменьшает ночные пробуждения\nСнижает уровень стресса",
                contraindications = "При острых психотических состояниях\nПри тяжёлой депрессии\nПри ПТСР"
            )
        )
    }

    private fun getNormalQualityPractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Визуализация спокойного места",
                shortDescription = "Отвлекает разум от тревожных мыслей",
                fullDescription = "Визуализация использует силу воображения для создания ментальных образов, которые вызывают чувство спокойствия и безопасности. Когда вы детально представляете успокаивающее место, мозг реагирует так, будто вы действительно там находитесь.",
                steps = "1. Лягте удобно, закройте глаза\n2. Сделайте 3 глубоких вдоха\n3. Представьте место, где вы чувствуете себя в полной безопасности\n4. Начните с визуальных деталей: какие цвета вы видите?\n5. Подключите звуки: шум прибоя, пение птиц, шелест листвы\n6. Добавьте запахи: солёный воздух, хвоя, цветы\n7. Ощутите тактильные ощущения: ветер на коже, тёплый песок\n8. Почувствуйте температуру: тепло солнца, прохладу тени\n9. Проведите в этом месте 5-10 минут, исследуя новые детали\n10. Перед выходом поблагодарите это место за спокойствие\n11. Медленно верните внимание в комнату, откройте глаза",
                duration = 10,
                category = "meditation",
                benefits = "Снижает тревожность\nОтвлекает от навязчивых мыслей\nУлучшает настроение\nПомогает при бессоннице\nМожет использоваться как якорь спокойствия",
                contraindications = "Не рекомендуется при диссоциативных расстройствах"
            )
        )
    }

    private fun getGoodQualityPractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Медитация благодарности",
                shortDescription = "Завершает день на позитивной ноте",
                fullDescription = "Практика благодарности помогает завершить день с ощущением удовлетворённости и покоя. Когда мы концентрируемся на хороших событиях дня, мозг снижает выработку кортизола и стрессовых гормонов.",
                steps = "1. Лягте удобно, закройте глаза\n2. Сделайте 3 глубоких вдоха\n3. Вспомните 3 хороших события сегодняшнего дня\n4. Поблагодарите за каждое событие\n5. Вспомните 3 человека, которым вы благодарны\n6. Почувствуйте тепло благодарности в груди\n7. Поблагодарите себя за что-то, что вы сделали сегодня\n8. Улыбнитесь\n9. Сделайте глубокий вдох и выдохните с облегчением\n10. День завершён, вы можете отдыхать спокойно",
                duration = 10,
                category = "meditation",
                benefits = "Снижает тревожность\nУлучшает настроение\nФормирует позитивное мышление\nСнижает уровень стресса\nУлучшает качество сна",
                contraindications = "При тяжёлой депрессии может быть сложно найти поводы для благодарности"
            )
        )
    }

    private fun getLowSleepHoursPractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Как увеличить продолжительность сна",
                shortDescription = "Стратегии для более долгого сна",
                fullDescription = "Вы спите меньше рекомендованного. Эти стратегии помогут постепенно увеличить продолжительность сна без стресса и сопротивления.",
                steps = "1. Ложитесь на 15 минут раньше каждый день в течение недели\n2. Установите напоминание о подготовке ко сну за час\n3. Создайте утренний ритуал, который мотивирует вставать\n4. Используйте приложения-будильники, отслеживающие фазы сна\n5. Уберите из спальни всё, что отвлекает\n6. Практикуйте расслабляющие техники перед сном\n7. Избегайте дневного сна или ограничьте его 20 минутами\n8. Увеличьте физическую активность днём\n9. Снижайте освещение за 2 часа до сна\n10. Отслеживайте прогресс в дневнике сна",
                duration = 0,
                category = "education",
                benefits = "Постепенное увеличение времени сна\nСнижение сопротивления режиму\nУлучшение качества сна\nПовышение дневной энергии",
                contraindications = "Нет"
            )
        )
    }

    private fun getHighSleepHoursPractices(): List<SleepPracticeEntity> {
        return listOf(
            SleepPracticeEntity(
                title = "Оптимизация продолжительности сна",
                shortDescription = "Как спать достаточно, но не слишком много",
                fullDescription = "Слишком долгий сон может быть так же вреден, как и недостаточный. Эти стратегии помогут вам найти оптимальную продолжительность и избежать пересыпания.",
                steps = "1. Установите фиксированное время пробуждения 7 дней в неделю\n2. Экспериментируйте с временем отхода ко сну\n3. Используйте умный будильник для пробуждения в лёгкой фазе\n4. Сразу вставайте после пробуждения, без кнопки 'ещё 5 минут'\n5. Обеспечьте яркий свет сразу после пробуждения\n6. Запланируйте активные дела на утро\n7. Избегайте сна дольше 20 минут днём\n8. Отслеживайте своё самочувствие при разной продолжительности сна\n9. Найдите свою оптимальную норму (обычно 7-8 часов)",
                duration = 0,
                category = "education",
                benefits = "Повышает энергию после пробуждения\nСнижает инертность сна\nУлучшает дневное самочувствие\nПомогает найти индивидуальную норму сна",
                contraindications = "Нет"
            )
        )
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    suspend fun savePractices(practices: List<SleepPracticeEntity>) {
        sleepDao.insertPractices(practices)
    }
    suspend fun generateAndSavePractices(profile: SleepProfileEntity) {
        val practices = generatePersonalizedPractices(profile)
        sleepDao.insertPractices(practices)
    }
}