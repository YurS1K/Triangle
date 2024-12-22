package ru.yarsu.models

enum class StatisticParams(
    private val type: String
){
    Color("color"),
    Type("type"),
    ColorType("color,type");
    companion object {
        fun getType(value: String): StatisticParams {
            for (enter in StatisticParams.entries) {
                if (enter.type == value) {
                    return enter
                }
            }
            throw IllegalArgumentException("Получено $value, но такого значения нет")
        }
    }
}
