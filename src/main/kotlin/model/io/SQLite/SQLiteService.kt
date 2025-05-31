package model.io.SQLite

import model.graph.GraphImpl
import model.graph.Graph
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.Result

class SQLiteService {
    private var dbPath: String? = null

    fun getDbPath(): String? {
        return dbPath
    }

    fun setDbPath(path: String) {
        dbPath = path
    }

    private fun initializeDatabaseAtPath(dbFilePath: String): Result<Unit> {
        return try {
            if (dbFilePath.isBlank()) {
                throw IllegalArgumentException("SQLite database path cannot be blank for initialization.")
            }
            SQLGraph(dbFilePath).initializeDatabase()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to initialize SQLite database at $dbFilePath", e))
        }
    }

    fun saveGraphToNewFile(graphToSave: Graph, targetDirectoryPath: String, targetFileName: String): Result<String> {
        if (targetDirectoryPath.isBlank()) {
            return Result.failure(IllegalArgumentException("Directory path cannot be blank."))
        }
        if (targetFileName.isBlank()) {
            return Result.failure(IllegalArgumentException("File name cannot be blank."))
        }

        val finalFileName = if (!targetFileName.contains(".")) "$targetFileName.db" else targetFileName
        val fullPath = File(targetDirectoryPath, finalFileName).absolutePath

        try {
            if (Files.exists(Paths.get(fullPath))) {
                return Result.failure(IllegalArgumentException("File already exists at: $fullPath. Please choose a different name or location."))
            }

            if (graphToSave !is GraphImpl) {
                return Result.failure(IllegalStateException("Graph is not a GraphImpl instance, cannot save to SQLite."))
            }
            initializeDatabaseAtPath(fullPath).getOrThrow()
            SQLGraph(fullPath).saveGraph(graphToSave)

            dbPath = fullPath
            return Result.success(fullPath)

        } catch (e: Exception) {
            return Result.failure(Exception("Failed to save graph to SQLite at $fullPath", e))
        }
    }

    fun saveGraphToCurrentFile(graphToSave: Graph): Result<Unit> {
        val path = dbPath
        if (path.isNullOrBlank()) {
            return Result.failure(IllegalStateException("No current SQLite database file set. Use 'Save As...' first."))
        }

        if (graphToSave !is GraphImpl) {
            return Result.failure(IllegalStateException("Graph is not a GraphImpl instance, cannot save to SQLite."))
        }

        return try {
            SQLGraph(path).saveGraph(graphToSave)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save graph to current SQLite file at $path", e))
        }
    }

    fun loadGraphFromFile(filePath: String): Result<GraphImpl> {
        if (filePath.isBlank()) {
            return Result.failure(IllegalArgumentException("File path cannot be blank for loading."))
        }

        return try {
            val loadedGraph = SQLGraph(filePath).loadGraph()
            if (loadedGraph != null) {
                dbPath = filePath
                Result.success(loadedGraph)
            } else {
                Result.failure(FileNotFoundOrInvalidFormatException("No graph data found in the file, or the file is not a valid graph database: $filePath"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load graph from SQLite at $filePath", e))
        }
    }

    class FileNotFoundOrInvalidFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
