package ru.yarsu.models

import java.util.UUID
import kotlin.math.max
import kotlin.math.sqrt

data class Template(
    val id: UUID,
    val sideA: Int,
    val sideB: Int,
    val sideC: Int,
) {
    val area: Double?
        get() {
            if(type == TriangleType.Invalid){
                return  null
            }
            val p = (sideA.toDouble() + sideB.toDouble() + sideC.toDouble()) / 2
            return sqrt(p * (p - sideA) * (p - sideB) * (p - sideC))
        }

    /*
    остроугольный (квадрат наибольшей стороны меньше суммы квадратов двух других)
прямоугольный (квадрат наибольшей стороны равен сумме квадратов двух других)
тупоугольный (квадрат наибольшей стороны больше суммы квадратов двух других)
     */
    val type: TriangleType
        get() {
            val sides = mutableListOf(sideA, sideB, sideC)
            val largestSide = sides.max()
            sides.remove(largestSide)
            val sidesSum = sides.sum()
            if (largestSide > sidesSum) {
                return TriangleType.Invalid
            }
            if (largestSide == sides.sum()) {
                return TriangleType.Section
            }
            if ((largestSide * largestSide) < (sides[0] * sides[0] + sides[1] * sides[1])) {
                return TriangleType.Sharp
            }
            if ((largestSide * largestSide) == (sides[0] * sides[0] + sides[1] * sides[1])) {
                return TriangleType.Rectangular
            }
            if ((largestSide * largestSide) > (sides[0] * sides[0] + sides[1] * sides[1])) {
                return TriangleType.Obtuse
            }
            return TriangleType.Invalid
        }
}
