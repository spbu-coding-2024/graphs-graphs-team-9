package model.io.SQLite

import model.graph.GraphImpl
import model.graph.Graph
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SQLiteService {
    private suspend fun initializeDatabaseAtPath(dbFilePath: String): Result<Unit> {
        return try {
            if (dbFilePath.isBlank()) {
                throw IllegalArgumentException("SQLite database path cannot be blank for initialization.")
            }
            withContext(Dispatchers.IO) {
                SQLGraph(dbFilePath).initializeDatabase()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to initialize SQLite database at $dbFilePath", e))
        }
    }

    suspend fun saveGraphToNewFile(graphToSave: Graph, targetDirectoryPath: String, targetFileName: String): Result<String> {
        if (targetDirectoryPath.isBlank()) {
            return Result.failure(IllegalArgumentException("Directory path cannot be blank."))
        }
        if (targetFileName.isBlank()) {
            return Result.failure(IllegalArgumentException("File name cannot be blank."))
        }

        val finalFileName = if (!targetFileName.contains(".")) "$targetFileName.db" else targetFileName
        val fullPath = File(targetDirectoryPath, finalFileName).absolutePath

        return try {
            withContext(Dispatchers.IO) {
                if (Files.exists(Paths.get(fullPath))) {
                    return@withContext Result.failure(IllegalArgumentException("File already exists at: $fullPath. Please choose a different name or location."))
                }

                if (graphToSave !is GraphImpl) {
                    return@withContext Result.failure(IllegalStateException("Graph is not a GraphImpl instance, cannot save to SQLite."))
                }
                initializeDatabaseAtPath(fullPath).getOrThrow()
                SQLGraph(fullPath).saveGraph(graphToSave)

                Result.success(fullPath)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save graph to SQLite at $fullPath", e))
        }
    }

    suspend fun saveGraphToCurrentFile(graphToSave: Graph, currentDbPath: String): Result<Unit> {
        if (currentDbPath.isBlank()) {
            return Result.failure(IllegalStateException("No current SQLite database file set. Use 'Save As...' first."))
        }

        if (graphToSave !is GraphImpl) {
            return Result.failure(IllegalStateException("Graph is not a GraphImpl instance, cannot save to SQLite."))
        }

        return try {
            withContext(Dispatchers.IO) {
                SQLGraph(currentDbPath).saveGraph(graphToSave)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save graph to current SQLite file at $currentDbPath", e))
        }
    }

    suspend fun loadGraphFromFile(filePath: String): Result<Pair<GraphImpl, String>> {
        if (filePath.isBlank()) {
            return Result.failure(IllegalArgumentException("File path cannot be blank for loading."))
        }

        return try {
            val loadedGraph = withContext(Dispatchers.IO) {
                SQLGraph(filePath).loadGraph()
            }
            if (loadedGraph != null) {
                Result.success(Pair(loadedGraph, filePath))
            } else {
                Result.failure(FileNotFoundOrInvalidFormatException("No graph data found in the file, or the file is not a valid graph database: $filePath"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load graph from SQLite at $filePath", e))
        }
    }

    class FileNotFoundOrInvalidFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
