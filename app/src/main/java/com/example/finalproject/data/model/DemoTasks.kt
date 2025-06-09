package com.example.finalproject.data.model

object DemoTasks {
    val tasks = listOf(
        Task(
            id = "1",
            title = "Implementar Login",
            description = "Implementar tela de login com autenticação",
            status = TaskStatus.COMPLETED,
            projectId = "1",
            assignedTo = listOf("user1", "user2"),
            createdBy = "user1",
            createdAt = "2025-06-09T10:00:00",
            updatedAt = "2025-06-09T10:00:00"
        ),
        Task(
            id = "2",
            title = "Design da UI",
            description = "Criar design system e componentes base",
            status = TaskStatus.ON_GOING,
            projectId = "1",
            assignedTo = listOf("user1"),
            createdBy = "user1",
            createdAt = "2025-06-09T11:00:00",
            updatedAt = "2025-06-09T11:00:00"
        ),
        Task(
            id = "3",
            title = "Testes Unitários",
            description = "Implementar testes unitários para as principais funcionalidades",
            status = TaskStatus.TO_DO,
            projectId = "1",
            assignedTo = listOf(),
            createdBy = "user1",
            createdAt = "2025-06-09T12:00:00",
            updatedAt = "2025-06-09T12:00:00"
        )
    )

    fun getTaskById(id: String): Task? {
        return tasks.find { it.id == id }
    }
}
