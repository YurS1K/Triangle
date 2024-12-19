package ru.yarsu.models

enum class Color(
    val color: String,
){
    Black("BLACK"),
    White("WHITE"),
    Red("RED"),
    Green("GREEN"),
    Blue("BLUE"),
    Yellow("YELLOW"),
    Cyan("CYAN"),
    Magenta("MAGENTA"),
    Silver("SILVER"),
    Gray("GRAY"),
    Maroon("MAROON"),
    Olive("OLIVE"),
    DarkGreen("DARKGREEN"),
    Purple("PURPLE"),
    Teal("TEAL");

    companion object{
        fun getType(value: String): Color{
            for (enter in Color.entries) {
                if (enter.color == value) {
                    return enter
                }
            }
            throw IllegalArgumentException("Получено $value, но такого значения нет")
        }
    }
}
