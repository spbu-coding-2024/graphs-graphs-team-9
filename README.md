![img.png](src/main/kotlin/viewModel/toosl/img.png)

Десктопное приложение для создания, визуализации и анализа графов. Этот проект, созданный с использованием Kotlin и
Jetpack Compose for Desktop, позволяет пользователям взаимодействовать с различными структурами графов и применять
распространенные алгоритмы на графах.

## Возможности

* **Создание и Манипулирование Графами:**
    * Создание ориентированных или неориентированных графов.
    * Создание взвешенных или невзвешенных графов.
    * Добавление и удаление вершин.
    * Добавление и удаление ребер (с необязательными весами).
* **Визуализация Графов:**
    * Интерактивное отображение графов с помощью Jetpack Compose.
    * Автоматическое размещение узлов графа с использованием алгоритма ForceAtlas2.
    * Регулируемый размер вершин.
    * Переключение видимости меток вершин и ребер.
* **Алгоритмы на Графах:**
    * **Алгоритм Дейкстры:** Поиск кратчайшего пути между двумя вершинами (для неотрицательных весов).
    * **Алгоритм Форда-Беллмана:** Поиск кратчайшего пути, поддержка ребер с отрицательным весом и обнаружение
      отрицательных циклов.
    * **Алгоритм Тарьяна:** Поиск сильно связанных компонент в ориентированном графе.
    * **Поиск Мостов:** Определение мостов в графе.
    * **Определение Ключевых Вершин:** Расчет гармонической центральности для поиска ключевых вершин.
* **Сохранение и Загрузка Данных:**
    * **SQLite:**
        * Сохранение текущего графа в новый файл базы данных SQLite (`.db`).
        * Сохранение изменений в существующий файл базы данных SQLite.
        * Загрузка графа из файла базы данных SQLite.
    * **Neo4j:**
        * Загрузка графа из базы данных Neo4j.
        * Сохранение текущего графа в базу данных Neo4j.
        * Очистка базы данных Neo4j.
* **Пользовательский Интерфейс:**
    * Интуитивно понятная панель меню для операций с графами и выбора алгоритмов.
    * Диалоговые окна для пользовательского ввода (например, детали вершин/ребер, учетные данные для баз данных).
    * Отображение результатов работы алгоритмов.

## Используемые Технологии

* **Язык:** Kotlin
* **UI Фреймворк:** Jetpack Compose for Desktop
* **Система Сборки:** Gradle
* **Базы Данных:**
    * SQLite (через `sqlite-jdbc`)
    * Neo4j (через `neo4j-java-driver`)
* **Алгоритм Размещения:** ForceAtlas2 (через Gephi Toolkit)
* **Тестирование:** JUnit 5, MockK, Kotlin Test, Jacoco для покрытия кода тестами.
* **CI (Непрерывная Интеграция):** GitHub Actions
* **Линтинг и форматирование:** ktlint (через pre-commit)

## Структура Проекта

Проект следует стандартной структуре Gradle, исходные файлы Kotlin организованы следующим образом:

```
graphs-graphs-team-9/
├── .github/           # Рабочие процессы GitHub Actions и правила для pull request'ов
├── gradle/            # Файлы Gradle wrapper
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── model/        # Основные модели данных и алгоритмы
│   │   │   │   ├── algorithms/ # Реализации алгоритмов на графах
│   │   │   │   ├── graph/      # Структуры данных графов (Vertex, Edge, Graph, GraphImpl)
│   │   │   │   └── io/         # Операции ввода/вывода (SQLite, Neo4j)
│   │   │   ├── view/         # Компоненты UI Jetpack Compose
│   │   │   │   ├── additionalButtons/ # Элементы UI для боковой панели
│   │   │   │   ├── additionalScreen/  # Диалоги и специфические экраны
│   │   │   │   └── graph/         # Компоненты для визуализации графа
│   │   │   ├── viewModel/    # ViewModel'и для логики UI и управления состоянием
│   │   │   └── Main.kt       # Главная точка входа приложения
│   │   └── resources/      # Ресурсы приложения
│   └── test/
│       └── kotlin/         # Модульные и интеграционные тесты
├── .gitignore
├── build.gradle.kts         # Основной скрипт сборки Gradle
├── gradlew                  # Исполняемый файл Gradle wrapper (Linux/macOS)
├── gradlew.bat              # Исполняемый файл Gradle wrapper (Windows)
├── LICENSE.                 # Лицензия проекта
├── .pre-commit-config.yaml  # Конфигурационный файл для предкоммитовой проверки
└── .editorconfig            # Конфигурационный файл для ktlint
```

## Начало Работы

### Предварительные требования

* JDK 18
* Python (для pre-commit hooks, если планируется разработка)

### Сборка Проекта

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/spbu-coding-2024/graphs-graphs-team-9.git
   cd graphs-graphs-team-9
   ```
2. Соберите проект:
   ```bash
   ./gradlew build
   ```

### Запуск Приложения

Вы можете запустить приложение, используя задачу Gradle `run`:

```bash
./gradlew run
```

## Тестирование

Для запуска юнит-тестов и интеграционных тестов:

```bash
./gradlew test
```

Отчеты о покрытии тестами (HTML) генерируются Jacoco и могут быть найдены в `build/reports/jacoco/test/html/index.html`
после запуска тестов.

## Создатели

+ [Богдан Филичкин](https://github.com/Bogban893)
+ [Кривоносов Константин](https://github.com/fUS1ONd)

## Для разработчиков

Для обеспечения единообразия стиля кода и раннего обнаружения проблем, в проекте используются pre-commit хуки.
Перед каждым коммитом необходимо проверять изменения при помощи этих хуков.

**Подробная инструкция по установке pre-commit:**

1. Установите `pre-commit` с помощью pip (если у вас еще не установлен pip и Python, установите их):
   ```bash
   pip install pre-commit
   ```
2. Активируйте pre-commit хуки для этого репозитория. Выполните эту команду в корневой директории проекта:
   ```bash
   pre-commit install
   ```

После выполнения этих шагов, перед каждым `git commit` будут автоматически запускаться настроенные проверки (включая
ktlint для форматирования и проверки Kotlin кода). Если проверки не пройдут, коммит будет прерван, и вам нужно будет
исправить ошибки, после чего снова попытаться сделать коммит.

Вы также можете запустить все проверки вручную для всех файлов:

```bash
pre-commit run --all-files
```

## Лицензия

Этот проект лицензирован под лицензией MIT. Подробности смотрите в файле [LICENSE.md](LICENSE.md).
