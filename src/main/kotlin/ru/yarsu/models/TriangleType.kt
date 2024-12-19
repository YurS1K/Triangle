package ru.yarsu.models

enum class TriangleType(
    val type: String,
){
    Invalid("некорректный"),
    Section("отрезок"),
    Sharp("остроугольный"),
    Rectangular("прямоугольный"),
    Obtuse("тупоугольный");

    companion object{
        fun getType(value: String): TriangleType{
            for (enter in TriangleType.entries) {
                if (enter.type == value) {
                    return enter
                }
            }
            throw IllegalArgumentException("Получено $value, но такого значения нет")
        }
    }
}
