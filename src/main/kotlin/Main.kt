package org.rockwood

import processing.core.PApplet

class BattleTerrainGen : PApplet() {
    private var color = 255f
    private var dir = false

    override fun setup() {
        frameRate(30f)
        background(255)
        val env = Environment()
        var numRivers = 0
        var hillIntensity = 0
        var sizeX = 10          // Size in standard DnD tiles -- 5 feet on an edge
        var sizeY = 10          // Size in standard DnD tiles -- 5 feet on an edge

        println(env.toString())
    }

    override fun draw() {
        background(color)
        if (color <= 0)
            dir = true
        else if (color >= 255)
            dir = false

        if (dir)
            color += 0.5f
        else
            color -= 0.5f
    }

    override fun settings() {
        size(800,600)
    }
}

fun main(args: Array<String>) {
    PApplet.main("org.rockwood.BattleTerrainGen")
}