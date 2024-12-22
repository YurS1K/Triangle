package ru.yarsu.storages

import ru.yarsu.models.User
import java.util.UUID

class UserStorage(
    private val userList: MutableList<User>,
) {
    fun getByID(id: UUID): User? = userList.find { it.id == id }

    fun add(user: User) {
        userList.add(user)
    }

    fun delete(user: User) {
        userList.removeIf { it.id == user.id }
    }

    fun getList(): List<User> = userList

    fun sortedWith(comparator: Comparator<in User>): List<User> = userList.sortedWith(comparator)

    fun getByLogin(login: String): User? = userList.find { it.login == login }
}
